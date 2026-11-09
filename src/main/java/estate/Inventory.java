package estate;

final class Inventory {
    private int grain = 10;
    private int millGrainDelivered;

    int grain() {
        return grain;
    }

    int millGrainDelivered() {
        return millGrainDelivered;
    }

    boolean allocateGrainToMill(int quantity) {
        if (quantity <= 0 || quantity > grain) {
            return false;
        }
        grain -= quantity;
        millGrainDelivered += quantity;
        return true;
    }
}
