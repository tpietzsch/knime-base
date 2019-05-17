/*******************************************************************************
 * Copyright (c) 2010 Haifeng Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.knime.base.node.mine.regression.gaussian.process.learner;

import java.io.Serializable;

import smile.math.kernel.GaussianKernel;
import smile.math.kernel.MercerKernel;
import smile.math.matrix.Cholesky;
import smile.math.matrix.DenseMatrix;
import smile.math.matrix.Matrix;
import smile.regression.Regression;

/**
 * Gaussian Process for Regression. A Gaussian process is a stochastic process whose realizations consist of random
 * values associated with every point in a range of times (or of space) such that each such random variable has a normal
 * distribution. Moreover, every finite collection of those random variables has a multivariate normal distribution.
 * <p>
 * A Gaussian process can be used as a prior probability distribution over functions in Bayesian inference. Given any
 * set of N points in the desired domain of your functions, take a multivariate Gaussian whose covariance matrix
 * parameter is the Gram matrix of N points with some desired kernel, and sample from that Gaussian. Inference of
 * continuous values with a Gaussian process prior is known as Gaussian process regression.
 * <p>
 * The fitting is performed in the reproducing kernel Hilbert space with the "kernel trick". The loss function is
 * squared-error. This also arises as the kriging estimate of a Gaussian random field in spatial statistics.
 * <p>
 * A significant problem with Gaussian process prediction is that it typically scales as O(n<sup>3</sup>). For large
 * problems (e.g. n &gt; 10,000) both storing the Gram matrix and solving the associated linear systems are prohibitive
 * on modern workstations. An extensive range of proposals have been suggested to deal with this problem. A popular
 * approach is the reduced-rank Approximations of the Gram Matrix, known as Nystrom approximation. Greedy approximation
 * is another popular approach that uses an active set of training points of size m selected from the training set of
 * size n &gt; m. We assume that it is impossible to search for the optimal subset of size m due to combinatorics. The
 * points in the active set could be selected randomly, but in general we might expect better performance if the points
 * are selected greedily w.r.t. some criterion. Recently, researchers had proposed relaxing the constraint that the
 * inducing variables must be a subset of training/test cases, turning the discrete selection problem into one of
 * continuous optimization.
 *
 * <h2>References</h2>
 * <ol>
 * <li>Carl Edward Rasmussen and Chris Williams. Gaussian Processes for Machine Learning, 2006.</li>
 * <li>Joaquin Quinonero-candela, Carl Edward Ramussen, Christopher K. I. Williams. Approximation Methods for Gaussian
 * Process Regression. 2007.</li>
 * <li>T. Poggio and F. Girosi. Networks for approximation and learning. Proc. IEEE 78(9):1484-1487, 1990.</li>
 * <li>Kai Zhang and James T. Kwok. Clustered Nystrom Method for Large Scale Manifold Learning and Dimension Reduction.
 * IEEE Transactions on Neural Networks, 2010.</li>
 * <li></li>
 * </ol>
 * 
 * @author Haifeng Li
 * @param <T>
 */
public class GaussianProcessRegression<T> implements Regression<T>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The control points in the regression.
     */
    private final double[][] m_knots;

    /**
     * The linear weights.
     */
    private final double[] m_w;

    /**
     * The distance functor.
     */
    private final MercerKernel<double[]> m_kernel;

    /**
     * The shrinkage/regularization parameter.
     */
    private final double m_lambda;

    private final DenseMatrix m_L;

    /**
     * Constructor. Fitting a regular Gaussian process model.
     * 
     * @param x the training dataset.
     * @param y the response variable.
     * @param kernel the Mercer kernel.
     * @param lambda the shrinkage/regularization parameter.
     */
    public GaussianProcessRegression(final double[][] x, final double[] y, final GaussianKernel kernel,
        final double lambda) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(
                String.format("The sizes of X and Y don't match: %d != %d", x.length, y.length));
        }
        if (lambda < 0.0) {
            throw new IllegalArgumentException("Invalid regularization parameter lambda = " + lambda);
        }

        this.m_kernel = kernel;
        this.m_lambda = lambda;
        this.m_knots = x;

        final int n = x.length;

        final DenseMatrix K = Matrix.zeros(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                final double k = kernel.k(x[i], x[j]);
                K.set(i, j, k);
                K.set(j, i, k);
            }

            K.add(i, i, lambda);
        }

        final Cholesky cholesky = K.cholesky();
        m_w = y.clone();
        cholesky.solve(m_w);
        m_L = cholesky.getL();
    }

    //    protected void train() {
    //        Cholesky cholesky = K.cholesky();
    //        cholesky.solve(w);
    //        L = cholesky.getL();
    //    }
    //
    //    protected void addEntry(final double[] x, final double y, final int iteration) {
    //        if (iteration == 0) {
    //            K = Matrix.zeros(x.length, x.length);
    //            knots = new double[x.length][x.length];
    //        }
    //        for (int i = 0; i < x.length; i++) {
    //            double k = kernel.k(new double[] {x[i]}, new double[] {y});
    //            K.set(i, iteration, k);
    //            K.set(iteration, i, k);
    //        }
    //        K.add(iteration, iteration, lambda);
    //        w[iteration] = y;
    //    }

    /**
     * @return returns lower triangular factor.
     */
    public DenseMatrix getL() {
        return m_L;
    }

    /**
     * @return the coefficients.
     */
    public double[] coefficients() {
        return m_w;
    }

    /**
     * @return the shrinkage parameter.
     */
    public double shrinkage() {
        return m_lambda;
    }

    @Override
    public double predict(final T x) {
        final double[] xDouble = (double[])x;
        double f = 0.0;

        final double[] kn = new double[m_knots.length];

        for (int i = 0; i < m_knots.length; i++) {
            kn[i] = m_kernel.k(xDouble, m_knots[i]);
            f += m_w[i] * kn[i];
        }
        return f;
    }

    /**
     * @param x The input vector to predict
     * @return the prediction
     */
    public double[] predictWithVariance(final double[] x) {
        double f = 0.0;

        final double[] kn = new double[m_knots.length];

        for (int i = 0; i < m_knots.length; i++) {
            kn[i] = m_kernel.k(x, m_knots[i]);
            f += m_w[i] * kn[i];
        }

        final int n = kn.length;

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < k; i++) {
                kn[k] -= kn[i] * m_L.get(k, i);
            }
            kn[k] /= m_L.get(k, k);
        }
        final double[] v = kn;
        double dot = 0.0;
        for (final double element : v) {
            dot += element * element;
        }
        final double[] ret = new double[2];
        ret[0] = f;
        ret[1] = m_kernel.k(x, x) - dot;

        return ret;
    }
}
