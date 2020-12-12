package morningsage.telecraft.data.builders;

@FunctionalInterface
public interface ParamCallback {
    void onParamFound(String name, String type);
}
