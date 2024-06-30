/*
 *
 *  Copyright 2014 David Hall
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package breeze.linalg

import breeze.benchmark._
import breeze.stats.distributions.RandBasis
import com.google.caliper.Benchmark

object SparseVectorBenchmark extends MyRunner(classOf[SparseVectorBenchmark])

class SparseVectorBenchmark extends BreezeBenchmark with BuildsRandomVectors {
  protected implicit val randBasis: RandBasis = RandBasis.mt0

  def timeAllocate(reps: Int) = run(reps) {
    SparseVector.zeros[Double](1024)
  }

  def dotProductBench(reps: Int, size: Int, sparsity: Double): Double = {
    dotProductBench(reps, size, sparsity, sparsity)
  }

  def dotProductBench(reps: Int, size: Int, sparsity1: Double, sparsity2: Double): Double = {
    runWith(reps, { (randomSparseVector(size, sparsity1), randomSparseVector(size, sparsity2)) }) {
      case (a, b) =>
        a.dot(b)
    }
  }

  @Benchmark
  def timeDotSmall1_%(reps: Int) = dotProductBench(reps, 1000, 0.01)

  @Benchmark
  def timeDotSmall10_%(reps: Int) = dotProductBench(reps, 1000, 0.10)

  @Benchmark
  def timeDotSmall30_%(reps: Int) = dotProductBench(reps, 1000, 0.30)

  @Benchmark
  def timeDotLargeUneven_10__0_1_%(reps: Int) = dotProductBench(reps, 1000000, 0.1, 0.001)

  @Benchmark
  def timeDotLargeUneven_10_1_%(reps: Int) = dotProductBench(reps, 1000000, 0.1, 0.01)

  @Benchmark
  def timeDotLargeUneven_10_30_%(reps: Int) = dotProductBench(reps, 1000000, 0.10, 0.3)

  @Benchmark
  def timeDotLargeUneven_1_30_%(reps: Int) = dotProductBench(reps, 1000000, 0.01, 0.3)

  @Benchmark
  def timeDotLarge1_%(reps: Int) = dotProductBench(reps, 1000000, 0.01)

  @Benchmark
  def timeDotLarge10_%(reps: Int) = dotProductBench(reps, 1000000, 0.10)

  @Benchmark
  def timeDotLarge30_%(reps: Int) = dotProductBench(reps, 1000000, 0.30)
}
