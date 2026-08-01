package org.data.definitions.assets;


import java.util.List;

public class InstrumentFactory {

    public static Instrument from(String instrument){

        List<Class<? extends Instrument>> classes = List.of(
                Stock.class,
                Indicator.class,
                FieldIndex.class,
                CountryIndex.class
        );

        for (Class<?> clazz : classes){

            if (clazz.isEnum()){
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum> classEnum = (Class<? extends Enum>) clazz;
                    Enum<?> enumeration = Enum.valueOf(classEnum, instrument);
                    return (Instrument) enumeration;
                } catch (Exception e){

                }
            }

        }
        return null;
    }

}
