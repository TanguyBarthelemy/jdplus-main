/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.math.splines;

import jdplus.toolkit.base.api.DemetraException;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class BSplines {

    public BSpline augmented(int order, double[] breaks) {
        return BSpline.of(order, breaks);
    }

    public BSpline periodic(int order, double[] breaks, double period) {
        return BSpline.ofPeriodic(order, breaks, period);
    }

    /**
     * Computes the matrix of regression variables corresponding to the provided
     * periodic b-spline
     *
     * @param spline The periodic b-spline
     * @param pos considered positions. In the case of periodic splines
     * (period=P, it should be in [0, P[. Otherwise, it should be between the
     * first and last knots (included). Not checked
     * @return
     */
    public FastMatrix splines(BSpline spline, DoubleSeq pos) {
        int dim = spline.dimension();
        FastMatrix M = FastMatrix.make(pos.length(), dim);
        DoubleSeqCursor cursor = pos.cursor();
        double[] B = new double[spline.getOrder()];
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()) {
            int left = spline.eval(cursor.getAndNext(), B);
            if (left<0)
                left+=dim;
            DataBlock row = rows.next();
            for (int i = 0; i < B.length; ++i) {
                row.set((i + left) % dim, B[i]);
            }
        }
        return M;
    }

    public static class BSpline {

        /**
         * Order of the spline
         */
        private final int k;
        /**
         * Number of internal polynomials
         */
        private final int l;
        /**
         * Number of knots added to the beginning of the breaks
         */
        private final int start;
        private final double[] knots;
        private final double period;

        private final double[] deltar, deltal;

        private BSpline(int k, int l, int start, double[] knots, double period) {
            this.k = k;
            this.l = l;
            this.start = start;
            this.knots = knots;
            this.deltal = new double[k];
            this.deltar = new double[k];
            this.period = period;
        }

        public boolean isPeriodic() {
            return period != 0;
        }

        public double getPeriod() {
            return period;
        }

        public int getOrder() {
            return k;
        }

        public DoubleSeq knots() {
            return DoubleSeq.of(knots);
        }

        public int dimension() {
            return period == 0 ? l + k - 1 : l + 1;
        }

        public int eval(double x, double[] B) {
            int end = findInterval(x);
            if (end < k - 1) {
                return -1;
            }
            pppack_bsplvb(x, end, B);

            return end - start; // first non null index
        }

        void pppack_bsplvb(final double x, int left, double[] biatx) {
            double saved;
            biatx[0] = 1.0;

            for (int j = 0; j < k - 1; ++j) {
                deltar[j] = knots[left + j + 1] - x;
                deltal[j] = x - knots[left - j];
                saved = 0.0;
                for (int i = 0; i <= j; ++i) {
                    double term = biatx[i] / (deltar[i]
                            + deltal[j - i]);
                    biatx[i] = saved + deltar[i] * term;
                    saved = deltal[j - i] * term;
                }

                biatx[j + 1] = saved;
            }
        }

        /**
         * Position of x in the knots (extended breaks). Find knot interval such
         * that t_i <= x < t_{i + 1}
         * Its position in the breaks is find(x)-(k-1) or find(x)-k (periodic splines with knots[0]>0)
         *
         *
         * @param x
         * @return
         */
        private int findInterval(final double x) {
            if (x < knots[0]) {
                return -1;
            }
            int imax = knots.length - 1;
            for (int i = k - 1; i < imax; i++) {
                double ti = knots[i], tip1 = knots[i + 1];

                if (tip1 < ti) {
                    throw new DemetraException("Invalid knots in B-Spline");
                }
                if (ti <= x && x < tip1) {
                    return i;
                }
            }
            return imax;
        }

        /* bspline_find_interval() */
        static BSpline of(int order, double[] breaks) {

            // Fill with k-1 * break[0] at the beginning
            // and k-i * break[l] at the end
            int k = order;
            int km1 = k - 1;
            int l = breaks.length - 1;
            int n = breaks.length + km1;
            double[] knots = new double[n + km1];
            for (int i = 0; i < km1; ++i) {
                knots[i] = breaks[0];
            }
            for (int i = 0, j = km1; i < breaks.length; ++i, ++j) {
                knots[j] = breaks[i];
            }
            for (int i = n; i < knots.length; i++) {
                knots[i] = breaks[l];
            }
            return new BSpline(k, l, km1, knots, 0);
        }

        static BSpline ofPeriodic(int order, double[] breaks, double P) {
            // Fill the beginning/end with the corresponding end/beginning of the breakpoints
            checkBreaks(breaks, P);
            int k = order;
            int km1 = k - 1;
            int l = breaks.length - 1;
            int start;
            if (Math.abs(breaks[0]) > 0) {
                start = k;
            } else {
                start = km1;
            }
            int n = breaks.length + km1;
            double[] knots = new double[n + start];
            // internal points
            for (int i = 0, j = start; i < breaks.length; ++i, ++j) {
                knots[j] = breaks[i];
            }
            // beginning
            for (int i = 0, j = breaks.length - start; i < start; ++i, ++j) {
                knots[i] = breaks[j] - P;
            }
            // end
            for (int i = breaks.length + start, j = 0; i < knots.length; ++i, ++j) {
                knots[i] = breaks[j] + P;
            }
            return new BSpline(k, l, start, knots, P);

        }

        private static void checkBreaks(double[] breaks, double P) {
            for (int i = 0; i < breaks.length; ++i) {
                if (breaks[i] < 0 || breaks[i] >= P) {
                    throw new IllegalArgumentException("Breaks should be in [0, P[");
                }
            }
        }

    }
}
