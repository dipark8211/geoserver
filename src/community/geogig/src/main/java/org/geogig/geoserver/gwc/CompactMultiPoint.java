/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geogig.geoserver.gwc;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Dimension;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

/**
 * A growable {@link MultiPoint} that saves all points in a single
 * {@link GrowableCoordinateSequence}
 */
class CompactMultiPoint extends MultiPoint {

    private static final long serialVersionUID = 1L;

    public static final GeometryFactory GEOM_FACTORY = new GeometryFactory(new PrecisionModel(1E6),
            0, new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.FLOAT));

    private final GrowableCoordinateSequence coordSeq;

    public CompactMultiPoint() {
        this(new GrowableCoordinateSequence(), new Envelope());
    }

    private CompactMultiPoint(GrowableCoordinateSequence coordSeq, Envelope envelope) {
        super(new Point[0], GEOM_FACTORY);
        super.envelope = envelope;
        this.coordSeq = coordSeq;
    }

    public void add(double x, double y) {
        coordSeq.add(x, y);
        super.envelope.expandToInclude(x, y);
    }

    // /////////////// GeometryCollection overrides ///////////////////////
    @Override
    public void apply(CoordinateFilter filter) {
        final int size = coordSeq.size();
        for (int i = 0; i < size; i++) {
            getGeometryN(i).apply(filter);
        }
    }

    @Override
    public void apply(CoordinateSequenceFilter filter) {
        final int size = coordSeq.size();
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            getGeometryN(i).apply(filter);
            if (filter.isDone()) {
                break;
            }
        }
        if (filter.isGeometryChanged()) {
            geometryChanged();
        }
    }

    @Override
    public void apply(GeometryComponentFilter filter) {
        filter.filter(this);
        final int size = coordSeq.size();
        for (int i = 0; i < size; i++) {
            getGeometryN(i).apply(filter);
        }
    }

    @Override
    public void apply(GeometryFilter filter) {
        filter.filter(this);
        final int size = coordSeq.size();
        for (int i = 0; i < size; i++) {
            getGeometryN(i).apply(filter);
        }
    }

    @Override
    public boolean equalsExact(Geometry other, double tolerance) {
        if (!(other instanceof MultiPoint)) {
            return false;
        }
        if (getNumGeometries() != other.getNumGeometries()) {
            return false;
        }
        for (int i = 0; i < getNumGeometries(); i++) {
            if (!getGeometryN(i).equalsExact(other.getGeometryN(i), tolerance)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object clone() {
        return new CompactMultiPoint(coordSeq.clone(), new Envelope(envelope));
    }

    @Override
    public double getArea() {
        return 0D;
    }

    @Override
    public boolean isEmpty() {
        return envelope.isNull();
    }

    @Override
    public int getDimension() {
        return 0;
    }

    @Override
    public int getBoundaryDimension() {
        return Dimension.FALSE;
    }

    @Override
    public Coordinate getCoordinate() {
        if (isEmpty()) {
            return null;
        }
        return coordSeq.getCoordinate(0);
    }

    @Override
    public Coordinate[] getCoordinates() {
        return coordSeq.toCoordinateArray();
    }

    @Override
    public Geometry getGeometryN(int n) {
        CoordinateSequence subSequence = coordSeq.subSequence(n, n);
        return GEOM_FACTORY.createPoint(subSequence);
    }

    @Override
    public double getLength() {
        return 0D;
    }

    @Override
    public int getNumGeometries() {
        return coordSeq.size();
    }

    @Override
    public int getNumPoints() {
        return getNumGeometries();
    }

    @Override
    public void normalize() {
        // nothing to do
    }

    @Override
    public Geometry reverse() {
        // nothing to do
        return (Geometry) clone();
    }

    // //////////////////// Geometry overrides ////////////////////////

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
