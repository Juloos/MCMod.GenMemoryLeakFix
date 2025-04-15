package juloos.gen_memory_leak_fix.mixin;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {

    @Final
    @Shadow
    private Long2ByteMap chunkToType;

    @Final
    @Shadow
    private PointOfInterestStorage pointOfInterestStorage;

    @Inject(method = "method_18843(Lnet/minecraft/server/world/ChunkHolder;Ljava/util/concurrent/CompletableFuture;JLnet/minecraft/world/chunk/Chunk;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;save(Lnet/minecraft/world/chunk/Chunk;)Z"))
    private void unloadChunkPOI(ChunkHolder chunkHolder, CompletableFuture<Chunk> completableFuture, long chunkLong, Chunk chunk, CallbackInfo ci) {
        chunkToType.remove(chunkLong);
        ChunkPos chunkPos = chunk.getPos();
        pointOfInterestStorage.saveChunk(chunkPos);
        int sectionPosMinY = ChunkSectionPos.getSectionCoord(chunk.getBottomY());
        for (int currentSectionY = 0; currentSectionY < chunk.countVerticalSections(); currentSectionY++) {
            long sectionPosKey = ChunkSectionPos.asLong(chunkPos.x, sectionPosMinY + currentSectionY, chunkPos.z);
            ((SerializingRegionBasedStorageAccessor<PointOfInterestSet>) pointOfInterestStorage).getLoadedElements().remove(sectionPosKey);
        }
    }

    @Inject(method = "method_18843(Lnet/minecraft/server/world/ChunkHolder;Ljava/util/concurrent/CompletableFuture;JLnet/minecraft/world/chunk/Chunk;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;unloadEntities(Lnet/minecraft/world/chunk/WorldChunk;)V"))
    private void unloadChunkBET(ChunkHolder chunkHolder, CompletableFuture<Chunk> completableFuture, long l, Chunk chunk, CallbackInfo ci) {
        ((WorldAccessor)((WorldChunk)chunk).getWorld()).getBlockEntityTickers().removeAll(((WorldChunkAccessor)chunk).getBlockEntityTickers().values());
    }
}
