package in.taguard.tempsense.able;

public interface ISlotDataAction {
    boolean isValid();

    void sendData();

    void resetParams();
}
