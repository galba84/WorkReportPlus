package com.example.workreportplus.ENUM;

public enum Rank {
    // Enlisted Ranks (OR)
    SOLDAT("Soldat", "Private", "OR-1"),
    STARSHYI_SOLDAT("Starshyi soldat", "Private 1st Class", "OR-2"),

    // Non-Commissioned Officer Ranks
    MOLODSHYI_SERZHANT("Molodshyi serzhant", "Junior Sergeant", "OR-3"),
    SERZHANT("Serzhant", "Sergeant", "OR-4"),
    STARSHYI_SERZHANT("Starshyi serzhant", "Senior Sergeant", "OR-5"),
    HOLOVNYI_SERZHANT("Holovnyi serzhant", "Chief Sergeant", "OR-6"),
    SHTAB_SERZHANT("Shtab-serzhant", "Staff Sergeant", "OR-7"),
    MAISTER_SERZHANT("Maister-serzhant", "Master Sergeant", "OR-8"),
    STARSHYI_MAISTER_SERZHANT("Starshyi maister-serzhant", "Senior Master Sergeant", "OR-9"),
    HOLOVNYI_MAISTER_SERZHANT("Holovnyi maister-serzhant", "Chief Master Sergeant", "OR-9"),

    // Officer Ranks
    MOLODSHIY_LEITENANT("Molodshiy leitenant", "Junior Lieutenant", "OF-1"),
    LEITENANT("Leitenant", "Lieutenant", "OF-1"),
    STARSHYI_LEITENANT("Starshyi leitenant", "Senior Lieutenant", "OF-2"),
    KAPITAN("Kapitan", "Captain", "OF-2"),
    MAIOR("Maior", "Major", "OF-3"),
    PIDPOLKOVNIK("Pidpolkovnik", "Lieutenant Colonel", "OF-4"),
    POLKOVNIK("Polkovnik", "Colonel", "OF-5"),
    BRIGADNYI_GENERAL("Brigadnyi general", "Brigadier General", "OF-6"),
    GENERAL_MAJOR("General-major", "Major General", "OF-7"),
    GENERAL_LEITENANT("General-leitenant", "Lieutenant General", "OF-8"),
    GENERAL_POLKOVNIK("General-polkovnik", "Colonel General", "OF-9"),
    GENERAL_ARMII("General armii", "General of the Army", "OF-10");

    private final String ukrainian;
    private final String english;
    private final String natoCode;

    Rank(String ukrainian, String english, String natoCode) {
        this.ukrainian = ukrainian;
        this.english = english;
        this.natoCode = natoCode;
    }

    public String getUkrainian() {
        return ukrainian;
    }

    public String getEnglish() {
        return english;
    }

    public String getNatoCode() {
        return natoCode;
    }
}
