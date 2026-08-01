package org.data.definitions.assets;

import lombok.Getter;
import org.series.ZoneIdEnum;

@Getter
public enum Stock implements Purchasable{
    SU("SU.PA", "Schneider Electric", Field.ELECTRICAL_EQUIPMENT,  Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    TTE("TTE.PA", "TotalEnergies", Field.OIL_GAS_CONSUMABLE_FUELS,  Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    MC("MC.PA", "LVMH", Field.TEXTILES_APPAREL_LUXURY_GOODS,  Country.FR, Currency.EUR,ZoneIdEnum.EUROPE_PARIS),
    AIR("AIR.PA", "Airbus", Field.AEROSPACE_DEFENSE,  Country.FR, Currency.EUR,ZoneIdEnum.EUROPE_PARIS),
    AI("AI.PA", "Air Liquide", Field.CHEMICALS,  Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SAF("SAF.PA", "Safran", Field.AEROSPACE_DEFENSE,  Country.FR, Currency.EUR,ZoneIdEnum.EUROPE_PARIS),
    SAN("SAN.PA", "Sanofi", Field.PHARMACEUTICALS,  Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    OR("OR.PA", "L'Oréal", Field.TEXTILES_APPAREL_LUXURY_GOODS,  Country.FR, Currency.EUR,ZoneIdEnum.EUROPE_PARIS);

    private final String ticker;
    private final String label;
    private final Field field;
    private final Country country;
    private final Currency currency;
    private final ZoneIdEnum zoneIdEnum;


    Stock(String ticker, String label, Field field, Country country, Currency currency, ZoneIdEnum zoneIdEnum) {
        this.ticker = ticker;
        this.country = country;
        this.field = field;
        this.label = label;
        this.currency = currency;
        this.zoneIdEnum = zoneIdEnum;
    }
}
