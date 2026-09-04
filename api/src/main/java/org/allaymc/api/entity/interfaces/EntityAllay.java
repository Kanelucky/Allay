package org.allaymc.api.entity.interfaces;

import org.allaymc.api.entity.component.EntityContainerHolderComponent;
import org.allaymc.api.entity.component.EntityFlyableComponent;
import org.allaymc.api.entity.component.EntityHeadYawComponent;
import org.allaymc.api.entity.component.EntityUndeadComponent;

public interface EntityAllay extends EntityIntelligent, EntityHeadYawComponent, EntityUndeadComponent, EntityContainerHolderComponent, EntityFlyableComponent {

}
