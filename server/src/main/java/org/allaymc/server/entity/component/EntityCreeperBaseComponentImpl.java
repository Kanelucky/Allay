package org.allaymc.server.entity.component;

import lombok.Getter;
import org.allaymc.api.entity.EntityInitInfo;
import org.allaymc.api.entity.component.EntityCreeperBaseComponent;
import org.cloudburstmc.nbt.NbtMap;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class EntityCreeperBaseComponentImpl extends EntityBaseComponentImpl implements EntityCreeperBaseComponent {

    public static final String TAG_FUSE = "Fuse";

    @Getter
    protected boolean swelling;

    @Getter
    protected int fuseTime = 0;

    public EntityCreeperBaseComponentImpl(EntityInitInfo initInfo) {
        super(initInfo);
    }

    @Override
    public AABBdc getBaseAABB() {
        return new AABBd(-0.3, 0.0, -0.3, 0.3, 1.7, 0.3);
    }

    @Override
    public void loadNBT(NbtMap nbt) {
        super.loadNBT(nbt);
        nbt.listenForShort(TAG_FUSE, this::setFuseTime);
    }

    @Override
    public NbtMap saveNBT() {
        return super.saveNBT()
                    .toBuilder()
                    .putShort(TAG_FUSE, (short) fuseTime)
                    .build();
    }

    @Override
    public void setSwelling(boolean swelling) {
        if (this.swelling == swelling) {
            return;
        }

        this.swelling = swelling;
        broadcastState();
    }

    @Override
    public void setFuseTime(int fuseTime) {
        this.fuseTime = fuseTime;
        broadcastState();
    }
}
