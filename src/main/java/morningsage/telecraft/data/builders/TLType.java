package morningsage.telecraft.data.builders;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TLType {
    CONSTRUCTOR("constructors"),
    METHOD("methods");

    @Getter private final String parseString;
}
