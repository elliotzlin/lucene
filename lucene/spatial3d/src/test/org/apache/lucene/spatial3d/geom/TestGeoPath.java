/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.spatial3d.geom;

import org.apache.lucene.tests.util.LuceneTestCase;
import org.junit.Test;

public class TestGeoPath extends LuceneTestCase {

  @Test
  public void testPathDistance() {
    // Start with a really simple case
    GeoStandardPath p;
    GeoPoint gp;
    p = new GeoStandardPath(PlanetModel.SPHERE, 0.1);
    p.addPoint(0.0, 0.0);
    p.addPoint(0.0, 0.1);
    p.addPoint(0.0, 0.2);
    p.done();
    gp = new GeoPoint(PlanetModel.SPHERE, Math.PI * 0.5, 0.15);
    assertEquals(Double.POSITIVE_INFINITY, p.computeDistance(DistanceStyle.ARC, gp), 0.0);
    gp = new GeoPoint(PlanetModel.SPHERE, 0.05, 0.15);
    assertEquals(0.15 + 0.05, p.computeDistance(DistanceStyle.ARC, gp), 0.000001);
    gp = new GeoPoint(PlanetModel.SPHERE, 0.0, 0.12);
    assertEquals(0.12 + 0.0, p.computeDistance(DistanceStyle.ARC, gp), 0.000001);
    gp = new GeoPoint(PlanetModel.SPHERE, -0.15, 0.05);
    assertEquals(Double.POSITIVE_INFINITY, p.computeDistance(DistanceStyle.ARC, gp), 0.000001);
    gp = new GeoPoint(PlanetModel.SPHERE, 0.0, 0.25);
    assertEquals(0.20 + 0.05, p.computeDistance(DistanceStyle.ARC, gp), 0.000001);
    gp = new GeoPoint(PlanetModel.SPHERE, 0.0, -0.05);
    assertEquals(0.0 + 0.05, p.computeDistance(DistanceStyle.ARC, gp), 0.000001);

    // Compute path distances now
    p = new GeoStandardPath(PlanetModel.SPHERE, 0.1);
    p.addPoint(0.0, 0.0);
    p.addPoint(0.0, 0.1);
    p.addPoint(0.0, 0.2);
    p.done();
    gp = new GeoPoint(PlanetModel.SPHERE, 0.05, 0.15);
    assertEquals(0.15 + 0.05, p.computeDistance(DistanceStyle.ARC, gp), 0.000001);
    assertEquals(0.15, p.computeNearestDistance(DistanceStyle.ARC, gp), 0.000001);
    assertEquals(0.10, p.computeDeltaDistance(DistanceStyle.ARC, gp), 0.000001);
    gp = new GeoPoint(PlanetModel.SPHERE, 0.0, 0.12);
    assertEquals(0.12, p.computeDistance(DistanceStyle.ARC, gp), 0.000001);
    assertEquals(0.12, p.computeNearestDistance(DistanceStyle.ARC, gp), 0.000001);
    assertEquals(0.0, p.computeDeltaDistance(DistanceStyle.ARC, gp), 0.000001);

    // Now try a vertical path, and make sure distances are as expected
    p = new GeoStandardPath(PlanetModel.SPHERE, 0.1);
    p.addPoint(-Math.PI * 0.25, -0.5);
    p.addPoint(Math.PI * 0.25, -0.5);
    p.done();
    gp = new GeoPoint(PlanetModel.SPHERE, 0.0, 0.0);
    assertEquals(Double.POSITIVE_INFINITY, p.computeDistance(DistanceStyle.ARC, gp), 0.0);
    gp = new GeoPoint(PlanetModel.SPHERE, -0.1, -1.0);
    assertEquals(Double.POSITIVE_INFINITY, p.computeDistance(DistanceStyle.ARC, gp), 0.0);
    gp = new GeoPoint(PlanetModel.SPHERE, Math.PI * 0.25 + 0.05, -0.5);
    assertEquals(Math.PI * 0.5 + 0.05, p.computeDistance(DistanceStyle.ARC, gp), 0.000001);
    gp = new GeoPoint(PlanetModel.SPHERE, -Math.PI * 0.25 - 0.05, -0.5);
    assertEquals(0.0 + 0.05, p.computeDistance(DistanceStyle.ARC, gp), 0.000001);
  }

  @Test
  public void test11956() {
    // Geo3D:GeoStandardPath: {planetmodel=PlanetModel.SPHERE, width=1.1344640137963142(65.0),
    //  points={[[lat=-1.289777264488089, lon=3.0020962766211765([X=-0.2746408902222561,
    // Y=0.0385618624109501, Z=-0.96077331571257])],
    // [lat=-1.50113114141284, lon=2.545709547022838([X=-0.057611985525967656,
    // Y=0.03906726187412952, Z=-0.997574362227405])],
    // [lat=1.079898704051346, lon=1.7302019835278628([X=-0.07482880413595766,
    // Y=0.46544097200827866, Z=0.8819100587064257])],
    // [lat=0.4651998030659944, lon=-1.731044309953635([X=-0.14260656697812318,
    // Y=-0.8822812296622808, Z=0.44860138078290357])],
    // [lat=-0.058395560871481914, lon=-1.467184843697817([X=0.10324990479626492,
    // Y=-0.9929417354486986, Z=-0.058362377981787186])]]}}
    // intersect Geo3D:GeoDegeneratePoint:
    //   {planetmodel=PlanetModel.SPHERE, lat=0.7332272528281016(42.01082701102197),
    // lon=0.7287424582438785(41.753867209362866)}
    // 1> XYZBounds of PathNode inconsistent with isWithin of children!
    // XYZBounds=XYZBounds: [xmin=-0.9626183326283182 xmax=0.8721428398024924
    // ymin=-0.8870855520255307 ymax=0.47448488489820667 zmin=0.06959538905950932 zmax=1.000000001]
    //  child1=CutoffDualCircleSegmentEndpoint: SegmentEndpoint ([lat=1.079898704051346,
    // lon=1.7302019835278628([X=-0.07482880413595766, Y=0.46544097200827866,
    // Z=0.8819100587064257])])
    //  child2=PathSegment ([X=0.8628106851303438, Y=0.11314342359669693, Z=0.4927030417216086],
    // [X=0.8341665648133144, Y=-0.4564285905826633, Z=0.30957888146040907], [X=-0.9547028437115205,
    // Y=-0.2893077287099765, Z=0.06959539006148753], [X=-0.9260587233944911, Y=0.28026428546938364,
    // Z=0.25271955032268706])
    //  Point=[0.5543009381999603, 0.49479972312729714, 0.6692710242523532]
    // 1> Child1: Bounds=XYZBounds: [xmin=-0.9260587243944911 xmax=0.8721428398024924
    // ymin=-0.024137541083565 ymax=0.4654409730082787 zmin=0.25271954932268703
    // zmax=0.8819100597064257] isWithin=true
    // 1> Child2: Bounds=XYZBounds: [xmin=-0.9626183326283182 xmax=0.862810686131634
    // ymin=-0.8870855520255307 ymax=0.47448488489820667 zmin=0.06959538905950932 zmax=1.000000001]
    // isWithin=false

    GeoStandardPath p;
    p = new GeoStandardPath(PlanetModel.SPHERE, 1.1344640137963142);
    p.addPoint(-1.289777264488089, 3.0020962766211765);
    p.addPoint(-1.50113114141284, 2.545709547022838);
    p.addPoint(1.079898704051346, 1.7302019835278628);
    p.addPoint(0.4651998030659944, -1.731044309953635);
    p.addPoint(-0.058395560871481914, -1.467184843697817);
    p.done();
    GeoPoint gp = new GeoPoint(0.5543009381999603, 0.49479972312729714, 0.6692710242523532);
    assertTrue(PlanetModel.SPHERE.pointOnSurface(gp));
    assertTrue(p.isWithin(gp));
    // XYZBounds bounds = new XYZBounds();
    // p.getBounds(bounds);
    // assertTrue(bounds.isWithin(gp));
  }

  @Test
  public void testPathPointWithin() {
    // Tests whether we can properly detect whether a point is within a path or not
    GeoStandardPath p;
    GeoPoint gp;
    p = new GeoStandardPath(PlanetModel.SPHERE, 0.1);
    // Build a diagonal path crossing the equator
    p.addPoint(-0.2, -0.2);
    p.addPoint(0.2, 0.2);
    p.done();
    // Test points on the path
    gp = new GeoPoint(PlanetModel.SPHERE, -0.2, -0.2);
    assertTrue(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, 0.0, 0.0);
    assertTrue(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, 0.1, 0.1);
    assertTrue(p.isWithin(gp));
    // Test points off the path
    gp = new GeoPoint(PlanetModel.SPHERE, -0.2, 0.2);
    assertFalse(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, -Math.PI * 0.5, 0.0);
    assertFalse(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, 0.2, -0.2);
    assertFalse(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, 0.0, Math.PI);
    assertFalse(p.isWithin(gp));
    // Repeat the test, but across the terminator
    p = new GeoStandardPath(PlanetModel.SPHERE, 0.1);
    // Build a diagonal path crossing the equator
    p.addPoint(-0.2, Math.PI - 0.2);
    p.addPoint(0.2, -Math.PI + 0.2);
    p.done();
    // Test points on the path
    gp = new GeoPoint(PlanetModel.SPHERE, -0.2, Math.PI - 0.2);
    assertTrue(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, 0.0, Math.PI);
    assertTrue(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, 0.1, -Math.PI + 0.1);
    assertTrue(p.isWithin(gp));
    // Test points off the path
    gp = new GeoPoint(PlanetModel.SPHERE, -0.2, -Math.PI + 0.2);
    assertFalse(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, -Math.PI * 0.5, 0.0);
    assertFalse(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, 0.2, Math.PI - 0.2);
    assertFalse(p.isWithin(gp));
    gp = new GeoPoint(PlanetModel.SPHERE, 0.0, 0.0);
    assertFalse(p.isWithin(gp));
  }

  @Test
  public void testGetRelationship() {
    GeoArea rect;
    GeoStandardPath p;
    GeoStandardPath c;
    GeoPoint point;
    PlanetModel planetModel;

    planetModel = new PlanetModel(1.151145876105594, 0.8488541238944061);
    c = new GeoStandardPath(planetModel, 0.008726646259971648);
    c.addPoint(-0.6925658899376476, 0.6316613927914589);
    c.addPoint(0.27828548161836364, 0.6785795524104564);
    c.done();
    point = new GeoPoint(planetModel, -0.49298555067758226, 0.9892440995026406);
    GeoAreaFactory.makeGeoArea(
        planetModel,
        0.49937141144985997,
        0.5161765426256085,
        0.3337218719537796,
        0.8544419570901649,
        -0.6347692823688085,
        0.3069696588119369);
    assertTrue(!c.isWithin(point));

    // Start by testing the basic kinds of relationship, increasing in order of difficulty.

    p = new GeoStandardPath(PlanetModel.SPHERE, 0.1);
    p.addPoint(-0.3, -0.3);
    p.addPoint(0.3, 0.3);
    p.done();
    // Easiest: The path is wholly contains the georect
    rect = new GeoRectangle(PlanetModel.SPHERE, 0.05, -0.05, -0.05, 0.05);
    assertEquals(GeoArea.CONTAINS, rect.getRelationship(p));
    // Next easiest: Some endpoints of the rectangle are inside, and some are outside.
    rect = new GeoRectangle(PlanetModel.SPHERE, 0.05, -0.05, -0.05, 0.5);
    assertEquals(GeoArea.OVERLAPS, rect.getRelationship(p));
    // Now, all points are outside, but the figures intersect
    rect = new GeoRectangle(PlanetModel.SPHERE, 0.05, -0.05, -0.5, 0.5);
    assertEquals(GeoArea.OVERLAPS, rect.getRelationship(p));
    // Finally, all points are outside, and the figures *do not* intersect
    rect = new GeoRectangle(PlanetModel.SPHERE, 0.5, -0.5, -0.5, 0.5);
    assertEquals(GeoArea.WITHIN, rect.getRelationship(p));
    // Check that segment edge overlap detection works
    rect = new GeoRectangle(PlanetModel.SPHERE, 0.1, 0.0, -0.1, 0.0);
    assertEquals(GeoArea.OVERLAPS, rect.getRelationship(p));
    rect = new GeoRectangle(PlanetModel.SPHERE, 0.2, 0.1, -0.2, -0.1);
    assertEquals(GeoArea.DISJOINT, rect.getRelationship(p));
    // Check if overlap at endpoints behaves as expected next
    rect = new GeoRectangle(PlanetModel.SPHERE, 0.5, -0.5, -0.5, -0.35);
    assertEquals(GeoArea.OVERLAPS, rect.getRelationship(p));
    rect = new GeoRectangle(PlanetModel.SPHERE, 0.5, -0.5, -0.5, -0.45);
    assertEquals(GeoArea.DISJOINT, rect.getRelationship(p));
  }

  @Test
  public void testPathBounds() {
    GeoStandardPath c;
    LatLonBounds b;
    XYZBounds xyzb;
    GeoPoint point;
    int relationship;
    GeoArea area;
    PlanetModel planetModel;

    planetModel = new PlanetModel(0.751521665790406, 1.248478334209594);
    c = new GeoStandardPath(planetModel, 0.7504915783575618);
    c.addPoint(0.10869761172400265, 0.08895880215465272);
    c.addPoint(0.22467878641991612, 0.10972973084229565);
    c.addPoint(-0.7398772468744732, -0.4465812941383364);
    c.addPoint(-0.18462055300079366, -0.6713857796763727);
    c.done();
    point = new GeoPoint(planetModel, -0.626645355125733, -1.409304625439381);
    xyzb = new XYZBounds();
    c.getBounds(xyzb);
    area =
        GeoAreaFactory.makeGeoArea(
            planetModel,
            xyzb.getMinimumX(),
            xyzb.getMaximumX(),
            xyzb.getMinimumY(),
            xyzb.getMaximumY(),
            xyzb.getMinimumZ(),
            xyzb.getMaximumZ());
    relationship = area.getRelationship(c);
    assertTrue(relationship == GeoArea.WITHIN || relationship == GeoArea.OVERLAPS);
    assertTrue(area.isWithin(point));
    // No longer true due to fixed GeoStandardPath waypoints.
    // assertTrue(zScaling.isWithin(point));

    c = new GeoStandardPath(PlanetModel.WGS84, 0.6894050545377601);
    c.addPoint(-0.0788176065762948, 0.9431251741731624);
    c.addPoint(0.510387871458147, 0.5327078872484678);
    c.addPoint(-0.5624521609859962, 1.5398841746888388);
    c.addPoint(-0.5025171434638661, -0.5895998642788894);
    c.done();
    point = new GeoPoint(PlanetModel.WGS84, 0.023652082107211682, 0.023131910152748437);
    // System.err.println("Point.x = "+point.x+"; point.y="+point.y+"; point.z="+point.z);
    assertTrue(c.isWithin(point));
    xyzb = new XYZBounds();
    c.getBounds(xyzb);
    area =
        GeoAreaFactory.makeGeoArea(
            PlanetModel.WGS84,
            xyzb.getMinimumX(),
            xyzb.getMaximumX(),
            xyzb.getMinimumY(),
            xyzb.getMaximumY(),
            xyzb.getMinimumZ(),
            xyzb.getMaximumZ());
    // System.err.println("minx="+xyzb.getMinimumX()+" maxx="+xyzb.getMaximumX()+"
    // miny="+xyzb.getMinimumY()+" maxy="+xyzb.getMaximumY()+" minz="+xyzb.getMinimumZ()+"
    // maxz="+xyzb.getMaximumZ());
    // System.err.println("point.x="+point.x+" point.y="+point.y+" point.z="+point.z);
    relationship = area.getRelationship(c);
    assertTrue(relationship == GeoArea.WITHIN || relationship == GeoArea.OVERLAPS);
    assertTrue(area.isWithin(point));

    c = new GeoStandardPath(PlanetModel.WGS84, 0.7766715171374766);
    c.addPoint(-0.2751718361148076, -0.7786721269011477);
    c.addPoint(0.5728375851539309, -1.2700115736820465);
    c.done();
    point = new GeoPoint(PlanetModel.WGS84, -0.01580760332365284, -0.03956004622490505);
    assertTrue(c.isWithin(point));
    xyzb = new XYZBounds();
    c.getBounds(xyzb);
    area =
        GeoAreaFactory.makeGeoArea(
            PlanetModel.WGS84,
            xyzb.getMinimumX(),
            xyzb.getMaximumX(),
            xyzb.getMinimumY(),
            xyzb.getMaximumY(),
            xyzb.getMinimumZ(),
            xyzb.getMaximumZ());
    // System.err.println("minx="+xyzb.getMinimumX()+" maxx="+xyzb.getMaximumX()+"
    // miny="+xyzb.getMinimumY()+" maxy="+xyzb.getMaximumY()+" minz="+xyzb.getMinimumZ()+"
    // maxz="+xyzb.getMaximumZ());
    // System.err.println("point.x="+point.x+" point.y="+point.y+" point.z="+point.z);
    relationship = area.getRelationship(c);
    assertTrue(relationship == GeoArea.WITHIN || relationship == GeoArea.OVERLAPS);
    assertTrue(area.isWithin(point));

    c = new GeoStandardPath(PlanetModel.SPHERE, 0.1);
    c.addPoint(-0.3, -0.3);
    c.addPoint(0.3, 0.3);
    c.done();

    b = new LatLonBounds();
    c.getBounds(b);
    assertFalse(b.checkNoLongitudeBound());
    assertFalse(b.checkNoTopLatitudeBound());
    assertFalse(b.checkNoBottomLatitudeBound());
    assertEquals(-0.4046919, b.getLeftLongitude(), 0.000001);
    assertEquals(0.4046919, b.getRightLongitude(), 0.000001);
    assertEquals(-0.3999999, b.getMinLatitude(), 0.000001);
    assertEquals(0.3999999, b.getMaxLatitude(), 0.000001);
  }

  @Test
  public void testCoLinear() {
    // p1: (12,-90), p2: (11, -55), (129, -90)
    GeoStandardPath p = new GeoStandardPath(PlanetModel.SPHERE, 0.1);
    p.addPoint(Math.toRadians(-90), Math.toRadians(12)); // south pole
    p.addPoint(Math.toRadians(-55), Math.toRadians(11));
    p.addPoint(Math.toRadians(-90), Math.toRadians(129)); // south pole again
    p.done(); // at least test this doesn't bomb like it used too -- LUCENE-6520
  }

  @Test
  public void testFailure1() {
    /*
    GeoStandardPath: {planetmodel=PlanetModel.WGS84, width=1.117010721276371(64.0), points={[
    [lat=2.18531083006635E-12, lon=-3.141592653589793([X=-1.0011188539924791, Y=-1.226017000107956E-16, Z=2.187755873813378E-12])],
    [lat=0.0, lon=-3.141592653589793([X=-1.0011188539924791, Y=-1.226017000107956E-16, Z=0.0])]]}}
     */
    final GeoPoint[] points =
        new GeoPoint[] {
          new GeoPoint(PlanetModel.WGS84, 2.18531083006635E-12, -3.141592653589793),
          new GeoPoint(PlanetModel.WGS84, 0.0, -3.141592653589793)
        };

    final GeoPath path;
    try {
      path = GeoPathFactory.makeGeoPath(PlanetModel.WGS84, 1.117010721276371, points);
    } catch (
        @SuppressWarnings("unused")
        IllegalArgumentException e) {
      return;
    }
    assertTrue(false);

    final GeoPoint point =
        new GeoPoint(PlanetModel.WGS84, -2.848117399637174E-91, -1.1092122135274942);
    System.err.println("point = " + point);

    final XYZBounds bounds = new XYZBounds();
    path.getBounds(bounds);

    final XYZSolid solid =
        XYZSolidFactory.makeXYZSolid(
            PlanetModel.WGS84,
            bounds.getMinimumX(),
            bounds.getMaximumX(),
            bounds.getMinimumY(),
            bounds.getMaximumY(),
            bounds.getMinimumZ(),
            bounds.getMaximumZ());

    assertTrue(path.isWithin(point));
    assertTrue(solid.isWithin(point));
  }

  @Test
  public void testInterpolation() {
    final double lat = 52.51607;
    final double lon = 13.37698;
    final double[] pathLats =
        new double[] {
          52.5355, 52.54, 52.5626, 52.5665, 52.6007, 52.6135, 52.6303, 52.6651, 52.7074
        };
    final double[] pathLons =
        new double[] {
          13.3634, 13.3704, 13.3307, 13.3076, 13.2806, 13.2484, 13.2406, 13.241, 13.1926
        };

    // Set up a point in the right way
    final GeoPoint carPoint =
        new GeoPoint(PlanetModel.SPHERE, Math.toRadians(lat), Math.toRadians(lon));
    // Create the path, but use a tiny width (e.g. zero)
    final GeoPoint[] pathPoints = new GeoPoint[pathLats.length];
    for (int i = 0; i < pathPoints.length; i++) {
      pathPoints[i] =
          new GeoPoint(
              PlanetModel.SPHERE, Math.toRadians(pathLats[i]), Math.toRadians(pathLons[i]));
    }
    // Construct a path with no width
    final GeoPath thisPath = GeoPathFactory.makeGeoPath(PlanetModel.SPHERE, 0.0, pathPoints);
    // Construct a path with a width
    final GeoPath legacyPath = GeoPathFactory.makeGeoPath(PlanetModel.SPHERE, 1e-6, pathPoints);
    // Compute the inside distance to the atPoint using zero-width path
    final double distance = thisPath.computeNearestDistance(DistanceStyle.ARC, carPoint);
    // Compute the inside distance using legacy path
    final double legacyDistance = legacyPath.computeNearestDistance(DistanceStyle.ARC, carPoint);
    // Compute the inside distance using the legacy formula
    final double oldFormulaDistance = thisPath.computeDistance(DistanceStyle.ARC, carPoint);
    // Compute the inside distance using the legacy formula with the legacy shape
    final double oldFormulaLegacyDistance = legacyPath.computeDistance(DistanceStyle.ARC, carPoint);

    // These should be about the same
    assertEquals(legacyDistance, distance, 1e-12);
    assertEquals(oldFormulaLegacyDistance, oldFormulaDistance, 1e-12);
    // This isn't true because example search center is off of the path.
    // assertEquals(oldFormulaDistance, distance, 1e-12);

  }

  @Test
  public void testInterpolation2() {
    final double lat = 52.5665;
    final double lon = 13.3076;
    final double[] pathLats =
        new double[] {
          52.5355, 52.54, 52.5626, 52.5665, 52.6007, 52.6135, 52.6303, 52.6651, 52.7074
        };
    final double[] pathLons =
        new double[] {
          13.3634, 13.3704, 13.3307, 13.3076, 13.2806, 13.2484, 13.2406, 13.241, 13.1926
        };

    final GeoPoint carPoint =
        new GeoPoint(PlanetModel.SPHERE, Math.toRadians(lat), Math.toRadians(lon));
    final GeoPoint[] pathPoints = new GeoPoint[pathLats.length];
    for (int i = 0; i < pathPoints.length; i++) {
      pathPoints[i] =
          new GeoPoint(
              PlanetModel.SPHERE, Math.toRadians(pathLats[i]), Math.toRadians(pathLons[i]));
    }

    // Construct a path with no width
    final GeoPath thisPath = GeoPathFactory.makeGeoPath(PlanetModel.SPHERE, 0.0, pathPoints);
    // Construct a path with a width
    final GeoPath legacyPath = GeoPathFactory.makeGeoPath(PlanetModel.SPHERE, 1e-6, pathPoints);

    // Compute the inside distance to the atPoint using zero-width path
    final double distance = thisPath.computeNearestDistance(DistanceStyle.ARC, carPoint);
    // Compute the inside distance using legacy path
    final double legacyDistance = legacyPath.computeNearestDistance(DistanceStyle.ARC, carPoint);

    // Compute the inside distance using the legacy formula
    final double oldFormulaDistance = thisPath.computeDistance(DistanceStyle.ARC, carPoint);
    // Compute the inside distance using the legacy formula with the legacy shape
    final double oldFormulaLegacyDistance = legacyPath.computeDistance(DistanceStyle.ARC, carPoint);

    // These should be about the same
    assertEquals(legacyDistance, distance, 1e-12);

    assertEquals(oldFormulaLegacyDistance, oldFormulaDistance, 1e-12);

    // Since the point we picked is actually on the path, this should also be true
    assertEquals(oldFormulaDistance, distance, 1e-12);
  }

  @Test
  public void testIdenticalPoints() {
    PlanetModel planetModel = PlanetModel.WGS84;
    GeoPoint point1 = new GeoPoint(planetModel, 1.5707963267948963, -2.4818290647609542E-148);
    GeoPoint point2 = new GeoPoint(planetModel, 1.570796326794895, -3.5E-323);
    GeoPoint point3 = new GeoPoint(planetModel, 4.4E-323, -3.1415926535897896);
    GeoPath path =
        GeoPathFactory.makeGeoPath(planetModel, 0, new GeoPoint[] {point1, point2, point3});
    GeoPoint point = new GeoPoint(planetModel, -1.5707963267948952, 2.369064805649877E-284);
    // If not filtered the point is wrongly in set
    assertFalse(path.isWithin(point));
    // If not filtered it throws error
    path = GeoPathFactory.makeGeoPath(planetModel, 1e-6, new GeoPoint[] {point1, point2, point3});
    assertFalse(path.isWithin(point));

    GeoPoint point4 = new GeoPoint(planetModel, 1.5, 0);
    GeoPoint point5 = new GeoPoint(planetModel, 1.5, 0);
    GeoPoint point6 = new GeoPoint(planetModel, 4.4E-323, -3.1415926535897896);
    // If not filtered creates a degenerated Vector
    path = GeoPathFactory.makeGeoPath(planetModel, 0, new GeoPoint[] {point4, point5, point6});
    path = GeoPathFactory.makeGeoPath(planetModel, 0.5, new GeoPoint[] {point4, point5, point6});
  }

  @Test
  public void testLUCENE8696() {
    GeoPoint[] points = new GeoPoint[4];
    points[0] = new GeoPoint(PlanetModel.WGS84, 2.4457272005608357E-47, 0.017453291479645996);
    points[1] = new GeoPoint(PlanetModel.WGS84, 2.4457272005608357E-47, 0.8952476719156919);
    points[2] = new GeoPoint(PlanetModel.WGS84, 2.4457272005608357E-47, 0.6491968536639036);
    points[3] = new GeoPoint(PlanetModel.WGS84, -0.7718789008737459, 0.9236607495528212);
    GeoPath path = GeoPathFactory.makeGeoPath(PlanetModel.WGS84, 1.3439035240356338, points);
    GeoPoint check = new GeoPoint(0.02071783020158524, 0.9523290535474472, 0.30699177256064203);
    // Map to surface point, to remove that source of confusion
    GeoPoint surfaceCheck = PlanetModel.WGS84.createSurfacePoint(check);
    /*
    [junit4]   1>   cycle: cell=12502 parentCellID=12500 x: -1658490249 TO 2147483041, y: 2042111310 TO 2147483041, z: -2140282940 TO 2140277970, splits: 1 queue.size()=1
    [junit4]   1>     minx=-0.7731590077686981 maxx=1.0011188539924791 miny=0.9519964046486451 maxy=1.0011188539924791 minz=-0.9977622932859775 maxz=0.9977599768255027
    [junit4]   1>     GeoArea.CONTAINS: now addAll
     */
    XYZSolid solid =
        XYZSolidFactory.makeXYZSolid(
            PlanetModel.WGS84,
            -0.7731590077686981,
            1.0011188539924791,
            0.9519964046486451,
            1.0011188539924791,
            -0.9977622932859775,
            0.9977599768255027);
    // Verify that the point is within it
    assertTrue(solid.isWithin(surfaceCheck));
    // Check the (surface) relationship
    int relationship = solid.getRelationship(path);
    if (relationship == GeoArea.CONTAINS) {
      // If relationship is CONTAINS then any point in the solid must also be within the path
      // If point is within solid, it must be within shape
      assertTrue(path.isWithin(surfaceCheck));
    }
  }
}
