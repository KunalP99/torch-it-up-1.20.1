package torchplacer;

public enum WoodTorchVariant {
    OAK      ("oak"),
    SPRUCE   ("spruce"),
    BIRCH    ("birch"),
    JUNGLE   ("jungle"),
    ACACIA   ("acacia"),
    DARK_OAK ("dark_oak"),
    MANGROVE ("mangrove"),
    CHERRY   ("cherry"),
    BAMBOO   ("bamboo"),
    CRIMSON  ("crimson"),
    WARPED   ("warped");

    public final String id;

    WoodTorchVariant(String id) {
        this.id = id;
    }
}
