/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.model.structures;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.BlockUpdate;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.HashMap;
import java.util.List;


/**
 * A collection of actual blocks and their relative positions, usually _not_ absolute positions in an existing world Can also store a
 * specific position that the collection should be oriented around rather than always a specific corner Useful for for blueprints and other
 * things that apply a particular set of expectations on a spot in a world
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class BlockCollection {
    private static final Logger logger = LoggerFactory.getLogger(BlockCollection.class);

    // TODO: Can this integrate better with BlockSelection, rather than need its keyset constructed into a BlockSelection for utility?

    /** Map of what blocks are in which positions */
    private final HashMap<BlockPosition, Block> _blocks = new HashMap<BlockPosition, Block>();

    /** A specific position to use for attaching to a spot in the world - for a tree this could be the bottom trunk block. */
    private BlockPosition _attachPos = new BlockPosition(0, 0, 0);

    /**
     * Simple return of the BlockPositions this collection holds in the form of a BlockSelection
     *
     * @return A BlockSelection containing the positions within this collection
     */
    public BlockSelection positions() {
        return new BlockSelection(_blocks.keySet());
    }

    /**
     * Get the Block that matches the given position
     *
     * @param pos The position we care about
     *
     * @return The block at the position
     */
    public Block getBlock(BlockPosition pos) {
        return _blocks.get(pos);
    }

    /**
     * Return the main collection
     *
     * @return The Position-Block map
     */
    public HashMap<BlockPosition, Block> getBlocks() {
        return _blocks;
    }

    /**
     * Simply forwards to the main build method including itself as the BlockCollection to draw blocks from
     *
     * @param provider The world to build the collection in
     * @param position The position to build the collection at (using the collection's attachment position)
     *
     * @return A BlockSelection containing the final positions the blocks were built at
     */
    public BlockSelection build(WorldProvider provider, BlockPosition position) {
        return build(provider, position, this);
    }

    /**
     * Builds and returns only the blocks in the collection that matches the blockName
     *
     * @param provider The world to build the collection in
     * @param position The position to build the collection at (using the collection's attachment position)
     * @param blockUri The uri of the blocks we want to filter by
     *
     * @return The BlockSelection for the built blocks matching the filter
     */
    public BlockSelection buildWithFilter(WorldProvider provider, BlockPosition position, BlockUri blockUri) {
        BlockCollection filteredBlocks = filter(blockUri);
        return build(provider, position, filteredBlocks);
    }

    /**
     * Create the contents of the collection at a specific position in the world, returning the absolute BlockPositions. This can thus be
     * used to for instance build a blueprint, then optionally sort out what was built where so different block types can be made reactive
     * to subsequent input (say a PortalComponent storing what's frame and what's portal)
     *
     * @param provider       The world to build the collection in
     * @param position       The position to build the collection at (using the collection's attachment position)
     * @param buildingBlocks The BlockCollection we are using to build with, which could be filtered or the main one here
     *
     * @return A BlockSelection containing the final positions the blocks were built at
     */
    public BlockSelection build(WorldProvider provider, BlockPosition position, BlockCollection buildingBlocks) {
        BlockSelection result = new BlockSelection();
        logger.debug("Going to build this collection into the world at {}, attaching at relative {}", position, _attachPos);
        //System.out.println(toString());

        //seems useless, commented out
        //List<BlockUpdate> updates = Lists.newArrayListWithCapacity(buildingBlocks.getBlocks().size());
        for (BlockPosition pos : buildingBlocks.getBlocks().keySet()) {
            //System.out.println("Processing block " + getBlock(pos) + " relative position " + pos);
            int x = position.x + pos.x - _attachPos.x;
            int y = position.y + pos.y - _attachPos.y;
            int z = position.z + pos.z - _attachPos.z;
            //System.out.println("This block is being placed at " + x + "," + y + "," + z);

            // TODO: Fix this up for concurrency
            provider.setBlock(x, y, z, buildingBlocks.getBlocks().get(pos), provider.getBlock(x, y, z));
            result.add(new BlockPosition(x, y, z));
        }
        return result;
    }

    /**
     * Returns a filtered BlockCollection only including Blocks matching the supplied name
     *
     * @param blockUri The uri of the Block we're interested in
     *
     * @return A BlockCollection only containing the interesting blocks
     */
    public BlockCollection filter(BlockUri blockUri) {
        BlockCollection filtered = new BlockCollection();
        for (BlockPosition pos : _blocks.keySet()) {
            Block b = _blocks.get(pos);
            if (b.getURI().equals(blockUri)) {
                //System.out.println("Block " + b + " matches the filter for " + blockName + " so adding it");
                filtered.addBlock(pos, b);
            }
        }
        return filtered;
    }

    /**
     * TODO: Can this be used by build to not duplicate code? Maybe a static version that takes a BlockCollection param? Calculates a
     * localized version of the BlockSelection in this Collection's map as per a given position
     *
     * @param localPos The local position we're going to localize against
     *
     * @return The localized BlockSelection
     */
    public BlockSelection getLocalizedSelection(BlockPosition localPos) {
        BlockSelection result = new BlockSelection();
        logger.debug("Going to localize this BlockCollection to position {}", localPos);
        for (BlockPosition pos : _blocks.keySet()) {
            //System.out.println("Processing block " + getBlock(pos) + " relative position " + pos);
            int x = localPos.x + pos.x - _attachPos.x;
            int y = localPos.y + pos.y - _attachPos.y;
            int z = localPos.z + pos.z - _attachPos.z;
            //System.out.println("Localized to " + x + "," + y + "," + z);
            result.add(new BlockPosition(x, y, z));
        }
        return result;
    }

    public void addBlock(BlockPosition pos, Block b) {
        _blocks.put(pos, b);
    }

    public BlockPosition getAttachPos() {
        return _attachPos;
    }

    public void setAttachPos(BlockPosition pos) {
        _attachPos = pos;
    }

    /**
     * Returns the width (x) of the backing BlockSelection's widest point (horizontally measured, not diagonally)
     *
     * @return int holding calculated width or -1 if there are no elements
     */
    public int calcWidth() {
        return positions().calcWidth();
    }

    /**
     * Returns the height (y) of the backing BlockCollection's highest point (vertically measured, not diagonally)
     *
     * @return int holding calculated height or -1 if there are no elements
     */
    public int calcHeight() {
        return positions().calcHeight();
    }

    /**
     * Returns the depth (z) of the backing BlockCollection's deepest point (horizontally measured, not diagonally)
     *
     * @return int holding calculated depth or -1 if there are no elements
     */
    public int calcDepth() {
        return positions().calcDepth();
    }

    @Override
    public String toString() {
        String result = "[[";
        for (BlockPosition pos : _blocks.keySet()) {
            result += pos + "-" + getBlock(pos) + "],[";
        }
        result = result.substring(0, result.length() - 2) + "],attachPos:" + _attachPos;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockCollection)) return false;

        BlockCollection that = (BlockCollection) o;

        if (_attachPos != null ? !_attachPos.equals(that._attachPos) : that._attachPos != null) return false;
        if (_blocks != null ? !_blocks.equals(that._blocks) : that._blocks != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _blocks != null ? _blocks.hashCode() : 0;
        result = 31 * result + (_attachPos != null ? _attachPos.hashCode() : 0);
        return result;
    }

    public void merge(BlockCollection collection2) {
        // Calculate offset regarding the attach position
        int dx = _attachPos.x - collection2._attachPos.x;
        int dy = _attachPos.y - collection2._attachPos.y;
        int dz = _attachPos.z - collection2._attachPos.z;

        HashMap<BlockPosition, Block> map = collection2.getBlocks();
        for (BlockPosition pos : map.keySet()) {
            _blocks.put(new BlockPosition(pos.x + dx, pos.y + dy, pos.z + dz), map.get(pos));
        }
        logger.debug("Merging completed.");
    }
}
