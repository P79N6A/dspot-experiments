/**
 * Copyright 2017 MovingBlocks
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
package org.terasology.world.block.shape;


import Yaw.CLOCKWISE_90;
import com.bulletphysics.util.ObjectArrayList;
import org.junit.Assert;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.management.AssetManager;
import org.terasology.math.Rotation;
import org.terasology.math.VecMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.bullet.shapes.BulletConvexHullShape;
import org.terasology.physics.shapes.CollisionShape;
import org.terasology.physics.shapes.ConvexHullShape;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.shapes.BlockShape;


public class BlockShapeTest extends TerasologyTestingEnvironment {
    private BlockManagerImpl blockManager;

    private AssetManager assetManager;

    @Test
    public void testConvexHull() {
        BlockShape blockShape = assetManager.getAsset("engine:halfSlope", BlockShape.class).get();
        CollisionShape shape = blockShape.getCollisionShape(Rotation.rotate(CLOCKWISE_90));
        Assert.assertEquals((shape instanceof ConvexHullShape), true);
        if (shape instanceof ConvexHullShape) {
            Vector3f[] test = new Vector3f[]{ new Vector3f(0.49999997F, 0.0F, 0.49999997F), new Vector3f((-0.49999997F), (-0.49999997F), 0.49999997F), new Vector3f(0.49999997F, (-0.49999997F), 0.49999997F), new Vector3f(0.49999997F, 0.0F, (-0.49999997F)), new Vector3f(0.49999997F, (-0.49999997F), (-0.49999997F)), new Vector3f((-0.49999997F), (-0.49999997F), (-0.49999997F)), new Vector3f(0.49999997F, (-0.49999997F), 0.49999997F), new Vector3f(0.49999997F, (-0.49999997F), (-0.49999997F)), new Vector3f(0.49999997F, 0.0F, (-0.49999997F)), new Vector3f(0.49999997F, 0.0F, 0.49999997F), new Vector3f(0.49999997F, (-0.49999997F), 0.49999997F), new Vector3f((-0.49999997F), (-0.49999997F), 0.49999997F), new Vector3f((-0.49999997F), (-0.49999997F), (-0.49999997F)), new Vector3f(0.49999997F, (-0.49999997F), (-0.49999997F)), new Vector3f(0.49999997F, 0.0F, (-0.49999997F)), new Vector3f((-0.49999997F), (-0.49999997F), (-0.49999997F)), new Vector3f((-0.49999997F), (-0.49999997F), 0.49999997F), new Vector3f(0.49999997F, 0.0F, 0.49999997F) };
            BulletConvexHullShape bulletConvexHullShape = ((BulletConvexHullShape) (shape));
            ObjectArrayList<javax.vecmath.Vector3f> points = getPoints();
            for (int x = 0; x < (points.size()); x++) {
                fuzzVectorTest(test[x], VecMath.from(points.get(x)));
            }
        }
    }
}
