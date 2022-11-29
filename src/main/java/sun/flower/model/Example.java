package sun.flower.model;

import java.util.Objects;

public class Example {

    public String name;

    public Example() {
        // For jackson
    }

    public Example(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object any) {
        return any == this || any instanceof Example && Objects.equals(((Example) any).name, this.name);
    }
}
