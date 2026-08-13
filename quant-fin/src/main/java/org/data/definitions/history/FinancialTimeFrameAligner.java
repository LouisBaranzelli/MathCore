package org.data.definitions.history;

import org.series.TimeTools;
import org.series.timeserie.TimeFrame;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;


public class FinancialTimeFrameAligner {
    /**
     * Aligneur temporel financier permettant de déterminer la date de clôture d'une période (TimeFrame).
     *
     * <p><b>Principe d'alignement :</b><br>
     * La classe applique un arrondi vers le bas (<i>floor</i>) vers le dernier moment <b>certain et scellé</b>
     * qui garantit l'inclusion intégrale des données de la période demandée.
     * </p>
     * Attention pour un time frame Jour, Semaine ou Mois, l'heure ne compte pas.
     * Ainsi une journée entamée est considérée comme une journée complete
     *
     * <p><b>Gestion des jours chômés (Week-ends & Jours fériés) :</b>
     * <ul>
     *   <li>Si la date interrogée tombe en dehors des heures/jours de cotation, le curseur est
     *       automatiquement ramené au minuit (<code>00:00:00</code>) marquant la fin du dernier jour travaillé.</li>
     *   <li>Les nanosecondes et secondes sont systématiquement réinitialisées à zéro.</li>
     * </ul>
     * </p>
     */
    public static ZonedDateTime alignFloor(ZonedDateTime dateTime, TimeFrame timeFrame) {
        ZonedDateTime truncated = dateTime.truncatedTo(ChronoUnit.SECONDS);

        return switch (timeFrame) {
            case MI  -> alignMinute(truncated, 1);
            case MI5 -> alignMinute(truncated, 5);
            case MI15-> alignMinute(truncated, 15);
            case MI30-> alignMinute(truncated, 30);
            case HR  -> alignHour(truncated);
            case D   -> alignDay(truncated);
            case WK  -> alignWeek(truncated);
        };
    }

    private static ZonedDateTime alignMinute(ZonedDateTime dt, int interval) {
        // Si nous sommes sur un jour non travaillé (ex: Samedi)
        if (!TradingBuisnessDayUtil.isBusinessDay(dt)) {
            return FinancialTimeFrameAligner.ensureBusinessDay(dt).plusDays(1).minusSeconds(interval * 60L);
        }

        int minute = dt.getMinute();
        int targetMinute = (minute / interval) * interval;
        return dt.withMinute(targetMinute).withSecond(0).withNano(0);
    }

    private static ZonedDateTime alignHour(ZonedDateTime dt) {
        if (!TradingBuisnessDayUtil.isBusinessDay(dt)) {
            return FinancialTimeFrameAligner.ensureBusinessDay(dt).plusDays(1).minusHours(1);
        }

        return dt.withMinute(0).withSecond(0).withNano(0);
    }

    private static ZonedDateTime alignDay(ZonedDateTime dt) {
        // Attention: dans ce cas, l'heure compte.
        // Si Mardi 14h -> retourne Lundi minuit -> minuit même si c'est la première heure du jours, inclut le jour entier
        ZonedDateTime midnight = dt.with(LocalTime.MIDNIGHT);
        // Si on est sur un jour non travaillé (Samedi, Dimanche, Férié)
        if (!TradingBuisnessDayUtil.isBusinessDay(dt)) {
            // Recule jusqu'au minuit qui suit immédiatement le dernier jour travaillé
            // (ex: Samedi 00:00:00 pour marquer la fin du Vendredi)
            return ensureBusinessDay(dt);
        }
        // Si c'est un jour ouvré (ex: Mardi 14h00 -> Mardi 00:00:00)
        return midnight;
    }

    private static ZonedDateTime alignWeek(ZonedDateTime dt) {
        ZonedDateTime midnight = dt.with(LocalTime.MIDNIGHT);

        ZonedDateTime nextOrSameFridayMidnight = midnight.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        ZonedDateTime endOfCurrentWeek = FinancialTimeFrameAligner.ensureBusinessDay(nextOrSameFridayMidnight); // Cas ou Vendredi Férié

        // Si nous sommes APRÈS la clôture de la semaine (ex: Vendedi dans la journée, Samedi/Dimanche)
        if (!midnight.isBefore(endOfCurrentWeek)) {
            return endOfCurrentWeek;
        }
        // Sinon (du Lundi au Vendredi), la semaine n'est pas finie : clôture de la semaine PRÉCÉDENTE
        ZonedDateTime previousFridayMidnight = nextOrSameFridayMidnight.minusWeeks(1);
        return FinancialTimeFrameAligner.ensureBusinessDay(previousFridayMidnight);
    }

    private static ZonedDateTime alignMonth(ZonedDateTime dt) {
        ZonedDateTime midnight = dt.with(LocalTime.MIDNIGHT);

        // 1. Déterminer la fin du dernier jour ouvré du mois en cours
        // (ex: Samedi 00:00 si le mois finit un Vendredi)
        ZonedDateTime lastDayOfMonth = midnight.with(TemporalAdjusters.lastDayOfMonth());
        ZonedDateTime endOfLastBizDayCurrentMonth = FinancialTimeFrameAligner.ensureBusinessDay(lastDayOfMonth);

        // 2. Si nous sommes APRÈS le dernier jour ouvré du mois (ex: le week-end qui clôture le mois)
        // le mois en cours est déjà clôturé !
        if (!midnight.isBefore(endOfLastBizDayCurrentMonth)) {
            return endOfLastBizDayCurrentMonth;
        }

        // 3. Sinon (en cours de mois), le mois n'est pas terminé :
        // On renvoie la clôture du MOIS PRÉCÉDENT.
        ZonedDateTime lastDayOfPreviousMonth = midnight.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return FinancialTimeFrameAligner.ensureBusinessDay(lastDayOfPreviousMonth);
    }

    /**
     * Si 'dt' est un jour chômé, recule jusqu'au premier minuit (00:00:00)
     * qui suit immédiatement le dernier jour travaillé.
     * Exemple : Si Samedi/Dimanche -> renvoie le Samedi à 00:00:00 (Fin du Vendredi).
     */
    private static ZonedDateTime getEndOfLastBusinessDay(ZonedDateTime dt) {
        ZonedDateTime current = dt.with(LocalTime.MIDNIGHT);

        // Tant que le jour PRÉCÉDENT n'est pas un jour travaillé, on continue de reculer.
        while (!TradingBuisnessDayUtil.isBusinessDay(current.minusDays(1))) {
            current = current.minusDays(1);
        }
        return current;
    }

    /**
     * Recule jour par jour jusqu'à trouver un jour ouvré à MINUIT.
     */
    private static ZonedDateTime ensureBusinessDay(ZonedDateTime dt) {
        ZonedDateTime current = dt;
        while (!TradingBuisnessDayUtil.isBusinessDay(current)) {
            current = current.minusDays(1).with(LocalTime.MIDNIGHT);
        }
        return current;
    }
}