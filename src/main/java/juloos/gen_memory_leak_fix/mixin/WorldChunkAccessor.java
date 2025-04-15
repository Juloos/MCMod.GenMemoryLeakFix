package juloos.gen_memory_leak_fix.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(WorldChunk.class)
public interface WorldChunkAccessor {
    @Accessor
    Map<BlockPos, WorldChunk.WrappedBlockEntityTickInvoker> getBlockEntityTickers();
}
