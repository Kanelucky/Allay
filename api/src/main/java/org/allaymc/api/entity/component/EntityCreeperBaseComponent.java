package org.allaymc.api.entity.component;

public interface EntityCreeperBaseComponent extends EntityBaseComponent {
    boolean isSwelling();

    void setSwelling(boolean swelling);

    /**
     * Get the fuse of the creeper entity.
     *
     * @return the fuse of the creeper entity
     */
    int getFuseTime();

    /**
     * Set the fuse of the creeper entity.
     *
     * @param fuseTime the fuse of the creeper entity
     */
    void setFuseTime(int fuseTime);
}
