package net.nullcoil.soulscorch.entity.client.ghaze;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.nullcoil.soulscorch.entity.ai.ghaze.Ghaze;
import net.nullcoil.soulscorch.sound.ModSounds;

/**
 * The Ghaze's search sound, looping for as long as it hunts a target it can't see. Follows
 * the Ghaze around, and stops the moment it sees its target again (it plays "Ghaze watches"
 * then), calms down, or is gone.
 *
 * <p>Started from the Ghaze's client tick when searching begins (see
 * {@code GhazeEntity.searchLoopStarter}), like a bee's buzz.
 */
@Environment(EnvType.CLIENT)
public class GhazeSearchSoundInstance extends AbstractTickableSoundInstance {
    private final Ghaze ghaze;
    /** Which search this loop belongs to; a newer search means a newer loop has taken over. */
    private final int loopId;

    public GhazeSearchSoundInstance(Ghaze ghaze) {
        super(ModSounds.GHAZE_SEARCH_LOOP, SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
        this.ghaze = ghaze;
        this.loopId = ghaze.getSearchLoopId();
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = ghaze.getX();
        this.y = ghaze.getY();
        this.z = ghaze.getZ();
    }

    @Override
    public void tick() {
        if (this.ghaze.isRemoved() || !this.ghaze.isSearching() || this.ghaze.getSearchLoopId() != this.loopId) {
            this.stop();
            return;
        }
        this.x = this.ghaze.getX();
        this.y = this.ghaze.getY();
        this.z = this.ghaze.getZ();
    }

    /** Keep the loop going while out of earshot, so it's there when you come back in range. */
    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return !this.ghaze.isSilent();
    }
}
