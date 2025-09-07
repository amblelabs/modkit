package mc.duzo.animation.generic;

public record AnimationInfo(VisibilityList render, Perspective perspective, Movement movement, Transform transform) {

    public enum Movement {
        ALLOW,
        DISABLE;
    }

    public enum Perspective {
        FIRST_PERSON(true, false),
        THIRD_PERSON_BACK(false, false),
        THIRD_PERSON_FRONT(false, true);
        private final boolean firstPerson;
        private final boolean frontView;

        Perspective(boolean firstPerson, boolean frontView) {
            this.firstPerson = firstPerson;
            this.frontView = frontView;
        }

        public boolean isFirstPerson() {
            return this.firstPerson;
        }

        public boolean isFrontView() {
            return this.frontView;
        }
    }

    public enum Transform {
        ALL,
        TARGETED;
    }
}
