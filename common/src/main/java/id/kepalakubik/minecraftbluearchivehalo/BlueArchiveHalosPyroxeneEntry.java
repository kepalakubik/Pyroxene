package id.kepalakubik.minecraftbluearchivehalo;

public enum BlueArchiveHalosPyroxeneEntry {
    HINA, TSURUGI, YUUKA, HOSHINO, ARU, ALICE, MIKU, IZUNA,
    SHIROKO, KOHARU, HASUMI, AZUSA, HIFUMI, MIDORI, MOMOI,
    MIKA, IROHA, NOA, HANAKO, MARI, SHIROKO_TERROR, SERIKA,
    HIKARI, NOZOMI, KANOE;

    private static final int BASE_CMD = 10751000;

    public String getName() { return name().toLowerCase(); }
    public int getCmd() {
        return BASE_CMD + ordinal() + 1;
    }
}