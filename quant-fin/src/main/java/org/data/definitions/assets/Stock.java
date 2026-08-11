package org.data.definitions.assets;

import lombok.Getter;
import org.series.ZoneIdEnum;

@Getter
public enum Stock implements Purchasable{
    AC("AC.PA", "Accor", Field.HOTELS_RESTAURANTS_LEISURE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    AI("AI.PA", "Air Liquide", Field.CHEMICALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    AIR("AIR.PA", "Airbus", Field.AEROSPACE_DEFENSE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
//    MT("MT.PA", "ArcelorMittal", Field.METALS_MINING, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    CS("CS.PA", "AXA", Field.INSURANCE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    BNP("BNP.PA", "BNP Paribas", Field.BANKS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    EN("EN.PA", "Bouygues", Field.CONSTRUCTION_ENGINEERING, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    CAP("CAP.PA", "Capgemini", Field.IT_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    CA("CA.PA", "Carrefour", Field.FOOD_STAPLES_RETAILING, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ACA("ACA.PA", "Crédit Agricole", Field.BANKS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    BN("BN.PA", "Danone", Field.FOOD_PRODUCTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    DSY("DSY.PA", "Dassault Systèmes", Field.SOFTWARE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    EDEN("EDEN.PA", "Edenred", Field.PROFESSIONAL_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ENGI("ENGI.PA", "Engie", Field.MULTI_UTILITIES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    EL("EL.PA", "EssilorLuxottica", Field.HEALTHCARE_EQUIPMENT_SUPPLIES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ERF("ERF.PA", "Eurofins Scientific", Field.LIFE_SCIENCES_TOOLS_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    RMS("RMS.PA", "Hermès International", Field.TEXTILES_APPAREL_LUXURY_GOODS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    KER("KER.PA", "Kering", Field.TEXTILES_APPAREL_LUXURY_GOODS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    OR("OR.PA", "L'Oréal", Field.PERSONAL_PRODUCTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    LR("LR.PA", "Legrand", Field.ELECTRICAL_EQUIPMENT, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    MC("MC.PA", "LVMH", Field.TEXTILES_APPAREL_LUXURY_GOODS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ML("ML.PA", "Michelin", Field.AUTO_COMPONENTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ORA("ORA.PA", "Orange", Field.DIVERSIFIED_TELECOMMUNICATION_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    RI("RI.PA", "Pernod Ricard", Field.BEVERAGES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    PUB("PUB.PA", "Publicis Groupe", Field.MEDIA, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    RNO("RNO.PA", "Renault", Field.AUTOMOBILES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SAF("SAF.PA", "Safran", Field.AEROSPACE_DEFENSE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SGO("SGO.PA", "Saint-Gobain", Field.BUILDING_PRODUCTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SAN("SAN.PA", "Sanofi", Field.PHARMACEUTICALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SU("SU.PA", "Schneider Electric", Field.ELECTRICAL_EQUIPMENT, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    GLE("GLE.PA", "Société Générale", Field.BANKS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    STLAP("STLAP.PA", "Stellantis", Field.AUTOMOBILES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    STMPA("STMPA.PA", "STMicroelectronics", Field.SEMICONDUCTORS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    TEP("TEP.PA", "Teleperformance", Field.PROFESSIONAL_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    HO("HO.PA", "Thales", Field.AEROSPACE_DEFENSE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    TTE("TTE.PA", "TotalEnergies", Field.OIL_GAS_CONSUMABLE_FUELS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    URW("URW.PA", "Unibail-Rodamco-Westfield", Field.EQUITY_REAL_ESTATE_INVESTMENT_TRUSTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VIE("VIE.PA", "Veolia Environnement", Field.MULTI_UTILITIES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    DG("DG.PA", "Vinci", Field.CONSTRUCTION_ENGINEERING, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VIV("VIV.PA", "Vivendi", Field.MEDIA, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),

    // --- CAC NEXT 20 ---
    ALO("ALO.PA", "Alstom", Field.MACHINERY, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
//    RKE("RKE.PA", "Arkema", Field.CHEMICALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    BIM("BIM.PA", "BioMérieux", Field.HEALTHCARE_EQUIPMENT_SUPPLIES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    BVI("BVI.PA", "Bureau Veritas", Field.PROFESSIONAL_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    AM("AM.PA", "Dassault Aviation", Field.AEROSPACE_DEFENSE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    FGG("FGR.PA", "Eiffage", Field.CONSTRUCTION_ENGINEERING, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    GFC("GFC.PA", "Gecina", Field.EQUITY_REAL_ESTATE_INVESTMENT_TRUSTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    GET("GET.PA", "Getlink", Field.TRANSPORTATION_INFRASTRUCTURE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    IPN("IPN.PA", "Ipsen", Field.PHARMACEUTICALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    LI("LI.PA", "Klépierre", Field.EQUITY_REAL_ESTATE_INVESTMENT_TRUSTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    RXL("RXL.PA", "Rexel", Field.TRADING_DISTRIBUTORS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    DIM("DIM.PA", "Sartorius Stedim Biotech", Field.LIFE_SCIENCES_TOOLS_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SCR("SCR.PA", "SCOR", Field.INSURANCE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SW("SW.PA", "Sodexo", Field.HOTELS_RESTAURANTS_LEISURE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SOI("SOI.PA", "Soitec", Field.SEMICONDUCTORS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SOLB("SOL0.F", "Solvay", Field.CHEMICALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SPIE("SPIE.PA", "SPIE", Field.CONSTRUCTION_ENGINEERING, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    TE("TE.PA", "Technip Energies", Field.CONSTRUCTION_ENGINEERING, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    UBI("UBI.PA", "Ubisoft Entertainment", Field.ENTERTAINMENT, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    WLN("WLN.PA", "Worldline", Field.IT_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),

    // --- CAC MID 60 ---
    AF("AF.PA", "Air France-KLM", Field.AIRLINES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ATE("ATE.PA", "Alten", Field.IT_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    AMUN("AMUN.PA", "Amundi", Field.CAPITAL_MARKETS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    APAM("7AA.HM", "Aperam", Field.METALS_MINING, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ARG("ARG.PA", "Argan", Field.EQUITY_REAL_ESTATE_INVESTMENT_TRUSTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    BEN("BEN.PA", "Beneteau", Field.LEISURE_PRODUCTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    BB("BB.PA", "Bic", Field.HOUSEHOLD_PRODUCTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    BOL("BOL.PA", "Bolloré", Field.INDUSTRIAL_CONGLOMERATES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    CARM("CARM.PA", "Carmila", Field.EQUITY_REAL_ESTATE_INVESTMENT_TRUSTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    CLARI("CLARI.PA", "Clariane", Field.HEALTHCARE_PROVIDERS_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    COFA("COFA.PA", "Coface", Field.INSURANCE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    COV("COV.PA", "Covivio", Field.EQUITY_REAL_ESTATE_INVESTMENT_TRUSTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    DBG("DBG.PA", "Derichebourg", Field.PROFESSIONAL_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ELIOR("ELIOR.PA", "Elior Group", Field.HOTELS_RESTAURANTS_LEISURE, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ELIS("ELIS.PA", "Elis", Field.PROFESSIONAL_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    RF("RF.PA", "Eurazeo", Field.CAPITAL_MARKETS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    EAPI("EAPI.PA", "Euroapi", Field.PHARMACEUTICALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ETL("ETL.PA", "Eutelsat Communications", Field.DIVERSIFIED_TELECOMMUNICATION_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    FNAC("FNAC.PA", "Fnac Darty", Field.SPECIALTY_RETAIL, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    FRVIA("FRVIA.PA", "Forvia", Field.AUTO_COMPONENTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    GTT("GTT.PA", "Gaztransport et Technigaz", Field.ENERGY_EQUIPMENT_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ICAD("ICAD.PA", "Icade", Field.EQUITY_REAL_ESTATE_INVESTMENT_TRUSTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    IDL("IDL.PA", "ID Logistics", Field.AIR_FREIGHT_LOGISTICS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    NK("NK.PA", "Imerys", Field.CONSTRUCTION_MATERIALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    ITP("ITP.PA", "Interparfums", Field.PERSONAL_PRODUCTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    IPS("IPS.PA", "Ipsos", Field.MEDIA, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    DEC("DEC.PA", "JCDecaux", Field.MEDIA, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    FII("FII.PA", "LISI", Field.BUILDING_PRODUCTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    MDM("MDM.PA", "Maisons du Monde", Field.SPECIALTY_RETAIL, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    MERY("MERY.PA", "Mercialys", Field.EQUITY_REAL_ESTATE_INVESTMENT_TRUSTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    MMT("MMT.PA", "Métropole TV", Field.MEDIA, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    NEX("NEX.PA", "Nexans", Field.ELECTRICAL_EQUIPMENT, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    NXI("NXI.PA", "Nexity", Field.REAL_ESTATE_MANAGEMENT_DEVELOPMENT, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    OPM("OPM.PA", "OPmobility", Field.AUTO_COMPONENTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    PEUG("PEUG.PA", "Peugeot Invest", Field.DIVERSIFIED_FINANCIAL_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    RBT("RBT.PA", "Robertet", Field.CHEMICALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    RUI("RUI.PA", "Rubis", Field.OIL_GAS_CONSUMABLE_FUELS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SK("SK.PA", "SEB", Field.HOUSEHOLD_DURABLES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SMCP("SMCP.PA", "SMCP", Field.TEXTILES_APPAREL_LUXURY_GOODS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    S30("S30.PA", "Solutions 30", Field.IT_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SOP("SOP.PA", "Sopra Steria Group", Field.IT_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    SDG("SDG.PA", "Synergie", Field.PROFESSIONAL_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    TFI("TFI.PA", "TF1", Field.MEDIA, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    TKO("TKO.PA", "Tikehau Capital", Field.CAPITAL_MARKETS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    TRI("TRI.PA", "Trigano", Field.AUTOMOBILES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VK("VK.PA", "Vallourec", Field.ENERGY_EQUIPMENT_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VLA("VLA.PA", "Valneva", Field.BIOTECHNOLOGY, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VANTI("VANTI.PA", "Vantiva", Field.TECHNOLOGY_HARDWARE_STORAGE_PERIPHERALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VRLA("VRLA.PA", "Verallia", Field.CONTAINERS_PACKAGING, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VCT("VCT.PA", "Vicat", Field.CONSTRUCTION_MATERIALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VIRP("VIRP.PA", "Virbac", Field.PHARMACEUTICALS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VLTSA("VLTSA.PA", "Voltalia", Field.INDEPENDENT_POWER_PRODUCERS_ENERGY_TRADERS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    VU("VU.PA", "VusionGroup", Field.ELECTRONIC_EQUIPMENT_INSTRUMENTS_COMPONENTS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    WAVE("WAVE.PA", "Wavestone", Field.IT_SERVICES, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    REND("MF.PA", "Wendel", Field.CAPITAL_MARKETS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS),
    XFAB("XFAB.PA", "X-Fab", Field.SEMICONDUCTORS, Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS);

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
