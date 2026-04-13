package torchplacer;

public enum TorchSource {
    BOTH,
    BAG_ONLY,
    INVENTORY_ONLY;

    public TorchSource next() {
        TorchSource[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public String getDisplayName() {
        return switch (this) {
            case BOTH -> "Bag, then Inventory";
            case BAG_ONLY -> "Bag Only";
            case INVENTORY_ONLY -> "Inventory Only";
        };
    }
}
