package breeze.generic

import breeze.generic.UFunc.InPlaceImpl
import breeze.generic.UFunc.InPlaceImpl2
import breeze.generic.UFunc.UImpl
import breeze.generic.UFunc.UImpl2
import breeze.linalg.operators.GenericOps
import breeze.linalg.operators.GenericOpsLowPrio3
import breeze.linalg.operators.HasOps
import breeze.linalg.support._

/*
 Copyright 2012 David Hall

 Licensed under the Apache License, Version 2.0 (the "License")
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/**
 * "Universal" Functions that mimic numpy's. A universal function is typically defined
 * on anything that supports elementwise maps.
 *
 * For example, exp is a UFunc: It just calls exp on all components of the passed in
 * object.
 *
 * Moreover, "operators" like [[breeze.linalg.operators.OpAdd]] are UFuncs as well,
 * with syntactic sugar provided by way of [[breeze.linalg.NumericOps]].
 *
 * Additional implementations can be added as implicits by extending a UFunc's
 * Impl, InPlaceImpl, or SinkImpl traits. For example, [[breeze.math.Complex]] extends [[breeze.numerics.log]]
 * with the following implicit:
 *
 * {{{
    implicit object logComplexImpl extends breeze.numerics.log.Impl[Complex, Complex] { def apply(v: Complex) = v.log }
 * }}}
 *
 *
 *
 *@author dlwh
 */
trait UFunc extends HasOps {
  final def apply[@specialized(Int, Double, Float) V, @specialized(Int, Double, Float) VR](v: V)(implicit
    impl: Impl[V, VR]
  ): VR = impl(v)

  final def apply[@specialized(Int, Double, Float) V1,
                  @specialized(Int, Double, Float) V2,
                  @specialized(Int, Double, Float) VR
  ](v1: V1, v2: V2)(implicit impl: Impl2[V1, V2, VR]): VR = impl(v1, v2)

  // if there are three arguments, the first one is almost always a vector or something,
  // so no point specializing.
  final def apply[V1,
                  @specialized(Int, Double, Float) V2,
                  @specialized(Int, Double, Float) V3,
                  @specialized(Int, Double, Float) VR
  ](v1: V1, v2: V2, v3: V3)(implicit impl: Impl3[V1, V2, V3, VR]): VR =
    impl(v1, v2, v3)

  final def apply[V1, V2, V3, V4, VR](v1: V1, v2: V2, v3: V3, v4: V4)(implicit impl: Impl4[V1, V2, V3, V4, VR]): VR =
    impl(v1, v2, v3, v4)

  final def inPlace[V](v: V)(implicit impl: UFunc.InPlaceImpl[this.type, V]): V = { impl(v); v }
  final def inPlace[V, V2](v: V, v2: V2)(implicit impl: UFunc.InPlaceImpl2[this.type, V, V2]): V = { impl(v, v2); v }
  final def inPlace[V, V2, V3](v: V, v2: V2, v3: V3)(implicit impl: UFunc.InPlaceImpl3[this.type, V, V2, V3]): V = {
    impl(v, v2, v3); v
  }

  final def withSink[S](s: S) = new UFunc.WithSinkHelp[this.type, S](s)

//  @implicitNotFound("Could not find an implicit implementation for this UFunc with arguments ${V}")
  type Impl[V, VR] = UFunc.UImpl[this.type, V, VR]
//  @implicitNotFound("Could not find an implicit implementation for this UFunc with arguments ${V1}, ${V2}")
  type Impl2[V1, V2, VR] = UFunc.UImpl2[this.type, V1, V2, VR]
//  @implicitNotFound("Could not find an implicit implementation for this UFunc with arguments ${V1}, ${V2}, ${V3}")
  type Impl3[V1, V2, V3, VR] = UFunc.UImpl3[this.type, V1, V2, V3, VR]
//  @implicitNotFound("Could not find an implicit implementation for this UFunc with arguments ${V1}, ${V2}, ${V3}, ${V4}")
  type Impl4[V1, V2, V3, V4, VR] = UFunc.UImpl4[this.type, V1, V2, V3, V4, VR]
//  @implicitNotFound("Could not find an implicit inplace implementation for this UFunc with arguments ${V}")
  type InPlaceImpl[V] = UFunc.InPlaceImpl[this.type, V]
//  @implicitNotFound("Could not find an inplace implicit implementation for this UFunc with arguments ${V1}, ${V2}")
  type InPlaceImpl2[V1, V2] = UFunc.InPlaceImpl2[this.type, V1, V2]
//  @implicitNotFound("Could not find an inplace implicit implementation for this UFunc with arguments ${V1}, ${V2}, ${V3}")
  type InPlaceImpl3[V1, V2, V3] = UFunc.InPlaceImpl3[this.type, V1, V2, V3]

  //  @implicitNotFound("Could not find an implicit with-sink implementation for this UFunc with arguments ${S} ${V}")
  type SinkImpl[S, V] = UFunc.SinkImpl[this.type, S, V]
  //  @implicitNotFound("Could not find an implicit with-sink  implementation for this UFunc with arguments ${S} ${V1}, ${V2}")
  type SinkImpl2[S, V1, V2] = UFunc.SinkImpl2[this.type, S, V1, V2]
  //  @implicitNotFound("Could not find an implicit with-sink implementation for this UFunc with arguments ${S} ${V1}, ${V2}, ${V3}")
  type SinkImpl3[S, V1, V2, V3] = UFunc.SinkImpl3[this.type, S, V1, V2, V3]

}

trait VariableUFunc[U <: UFunc, T <: VariableUFunc[U, T]] { self: T =>
  final def apply[@specialized(Int, Double, Float) V, @specialized(Int, Double, Float) VR](v: V)(implicit
    impl: UFunc.UImpl2[U, T, V, VR]
  ): VR = impl(self, v)

  final def apply[@specialized(Int, Double, Float) V1,
                  @specialized(Int, Double, Float) V2,
                  @specialized(Int, Double, Float) VR
  ](v1: V1, v2: V2)(implicit impl: UFunc.UImpl3[U, T, V1, V2, VR]): VR =
    impl(self, v1, v2)

  final def apply[V1, V2, V3, VR](v1: V1, v2: V2, v3: V3)(implicit impl: UFunc.UImpl4[U, T, V1, V2, V3, VR]): VR = {
    impl(self, v1, v2, v3)
  }
}

// TODO: docs
trait ShapedUFunc extends UFunc

trait ElementwiseUFunc extends UFunc
trait MappingUFunc extends ElementwiseUFunc
trait ZeroPreservingUFunc extends ElementwiseUFunc

trait MappingUFuncOps extends MappingUFuncLowPrio with GenericOps {

  implicit def canZipMapValuesImpl_T[Tag <: MappingUFunc, T, V1, VR, U](implicit
    handhold: ScalarOf[T, V1],
    impl: UFunc.UImpl2[Tag, V1, V1, VR],
    canZipMapValues: CanZipMapValues[T, V1, VR, U]
  ): UFunc.UImpl2[Tag, T, T, U] = { (v1: T, v2: T) =>
    canZipMapValues.map(v1, v2, impl.apply)
  }

  implicit def canTransformValuesUFunc_T[Tag <: MappingUFunc, T, V](implicit
    canTransform: CanTransformValues[T, V],
    impl: UImpl[Tag, V, V]
  ): InPlaceImpl[Tag, T] = { (v: T) =>
    {
      canTransform.transform(v, impl.apply)
    }
  }

  implicit def canTransformValuesUFunc2_T[Tag <: MappingUFunc, T, V, V2](implicit
    canTransform: CanTransformValues[T, V],
    impl: UImpl2[Tag, V, V2, V]
  ): InPlaceImpl2[Tag, T, V2] = { (v: T, v2: V2) =>
    {
      canTransform.transform(v, impl.apply(_, v2))
    }
  }

  implicit def fromLowOrderCanMapValues[Op <: MappingUFunc, T, V, V2, U](implicit
    handhold: ScalarOf[T, V],
    impl: UFunc.UImpl[Op, V, V2],
    canMapValues: CanMapValues[T, V, V2, U]
  ): UFunc.UImpl[Op, T, U] = { (v: T) =>
    canMapValues.map(v, impl.apply)
  }

  implicit def canMapV1DV[Op <: MappingUFunc, T, V1, V2, VR, U](implicit
    handhold: ScalarOf[T, V1],
    impl: UFunc.UImpl2[Op, V1, V2, VR],
    canMapValues: CanMapValues[T, V1, VR, U]
  ): UFunc.UImpl2[Op, T, V2, U] = { (v1: T, v2: V2) =>
    canMapValues.map(v1, impl.apply(_, v2))
  }

}

sealed trait MappingUFuncLowPrio extends GenericOps {
  implicit def canMapV2Values[Op <: MappingUFunc, T, V1, V2, VR, U](implicit
    handhold: ScalarOf[T, V2],
    impl: UFunc.UImpl2[Op, V1, V2, VR],
    canMapValues: CanMapValues[T, V2, VR, U]
  ): UFunc.UImpl2[Op, V1, T, U] = { (v1: V1, v2: T) =>
    canMapValues.map(v2, impl.apply(v1, _))
  }

}

trait ZeroPreservingUFuncOps extends ZeroPreservingUFuncLowPrio with MappingUFuncOps {
  implicit def fromLowOrderCanMapActiveValues[Op <: ZeroPreservingUFunc, T, V, V2, U](implicit
    handhold: ScalarOf[T, V],
    impl: UFunc.UImpl[Op, V, V2],
    canMapValues: CanMapValues[T, V, V2, U]
  ): UFunc.UImpl[Op, T, U] = { (v: T) =>
    canMapValues.mapActive(v, impl.apply)
  }

  implicit def canMapActiveV1DV[Op <: ZeroPreservingUFunc, T, V1, V2, VR, U](implicit
    handhold: ScalarOf[T, V1],
    impl: UFunc.UImpl2[Op, V1, V2, VR],
    canMapValues: CanMapValues[T, V1, VR, U]
  ): UFunc.UImpl2[Op, T, V2, U] = { (v1: T, v2: V2) =>
    canMapValues.mapActive(v1, impl.apply(_, v2))
  }

  implicit def canTransformActiveValuesUFunc[Tag <: ZeroPreservingUFunc, T, V](implicit
    canTransform: CanTransformValues[T, V],
    impl: UImpl[Tag, V, V]
  ): InPlaceImpl[Tag, T] = { (v: T) =>
    canTransform.transformActive(v, impl.apply)
  }

  implicit def canTransformActiveValuesUFunc2_T[Tag <: ZeroPreservingUFunc, T, V, V2](implicit
    canTransform: CanTransformValues[T, V],
    impl: UImpl2[Tag, V, V2, V]
  ): InPlaceImpl2[Tag, T, V2] = { (v: T, v2: V2) =>
    {
      canTransform.transformActive(v, impl.apply(_, v2))
    }
  }

}

sealed trait ZeroPreservingUFuncLowPrio extends MappingUFuncOps {
  implicit def canMapV2ActiveValues[Op <: ZeroPreservingUFunc, T, V1, V2, VR, U](implicit
    handhold: ScalarOf[T, V2],
    impl: UFunc.UImpl2[Op, V1, V2, VR],
    canMapValues: CanMapValues[T, V2, VR, U]
  ): UFunc.UImpl2[Op, V1, T, U] = { (v1: V1, v2: T) =>
    canMapValues.mapActive(v2, impl.apply(v1, _))
  }

}

object UFunc {
  def apply[A1, R](f: A1 => R) = WrappedUFunc1(f)
  def apply[A1, A2, R](f: (A1, A2) => R) = WrappedUFunc2(f)

//  @implicitNotFound("Could not find an implicit implementation for ${Tag} with arguments ${V}")
  trait UImpl[Tag, @specialized(Int, Double, Float) V, @specialized(Int, Double, Float) +VR] extends Serializable {
    def apply(v: V): VR
  }

//  @implicitNotFound("Could not find an implicit implementation for ${Tag} with arguments ${V1}, ${V2}")
  trait UImpl2[Tag,
               @specialized(Int, Double, Float) V1,
               @specialized(Int, Double, Float) V2,
               @specialized(Int, Double, Float) +VR
  ] extends Serializable {
    def apply(v: V1, v2: V2): VR
  }

//  @implicitNotFound("Could not find an implicit implementation for ${Tag} with arguments ${V1}, ${V2}, ${V3}")
  trait UImpl3[Tag,
               @specialized(Int, Double, Float) V1,
               @specialized(Int, Double, Float) V2,
               @specialized(Int, Double, Float) V3,
               @specialized(Int, Double, Float) +VR
  ] extends Serializable {
    def apply(v: V1, v2: V2, v3: V3): VR
  }

//  @implicitNotFound("Could not find an implicit implementation for ${Tag} with arguments ${V1}, ${V2}, ${V3}, ${V4}")
  trait UImpl4[Tag,
               @specialized(Int, Double, Float) V1,
               @specialized(Int, Double, Float) V2,
               @specialized(Int, Double, Float) V3,
               @specialized(Int, Double, Float) V4,
               @specialized(Int, Double, Float) +VR
  ] extends Serializable {
    def apply(v: V1, v2: V2, v3: V3, v4: V4): VR
  }

//  @implicitNotFound("Could not find an implicit inplace implementation for ${Tag} with arguments ${V}")
  trait InPlaceImpl[Tag, V] extends Serializable {
    def apply(v: V): Unit
  }

//  @implicitNotFound("Could not find an implicit inplace implementation for ${Tag} with arguments ${V}, ${V2}")
  trait InPlaceImpl2[Tag, V, @specialized(Int, Double, Float) V2] extends Serializable {
    def apply(v: V, v2: V2): Unit
  }

//  @implicitNotFound("Could not find an implicit inplace implementation for ${Tag} with arguments ${V}, ${V2}, ${V3}")
  trait InPlaceImpl3[Tag, V, V2, V3] extends Serializable {
    def apply(v: V, v2: V2, v3: V3): Unit
  }

//  @implicitNotFound("Could not find an implicit inplace implementation for ${Tag} with arguments ${V}")
  trait SinkImpl[Tag, S, V] extends Serializable {
    def apply(sink: S, v: V): Unit
  }

//  @implicitNotFound("Could not find an implicit inplace implementation for ${Tag} with arguments ${V}, ${V2}")
  trait SinkImpl2[Tag, S, V, @specialized(Int, Double, Float) V2] extends Serializable {
    def apply(sink: S, v: V, v2: V2): Unit
  }

//  @implicitNotFound("Could not find an implicit inplace implementation for ${Tag} with arguments ${V}, ${V2}, ${V3}")
  trait SinkImpl3[Tag, S, V, V2, V3] extends Serializable {
    def apply(sink: S, v: V, v2: V2, v3: V3): Unit
  }

//  implicit def canMapToSinkValuesUFunc[Tag, S, T, V, V2](
//      implicit scalar: ScalarOf[T, V],
//      canMapToSink: mapValues.SinkImpl2[S, T, V => V2],
//      impl: UImpl[Tag, V, V2]): SinkImpl[Tag, S, T] = {
//    new SinkImpl[Tag, S, T] {
//      def apply(sink: S, v: T) = { canMapToSink(sink, v, impl.apply) }
//    }
//  }

  final class WithSinkHelp[Tag, S](val __s: S) extends AnyVal {
    def apply[V](v: V)(implicit impl: UFunc.SinkImpl[Tag, S, V]): S = { impl(__s, v); __s }
    def apply[V, V2](v: V, v2: V2)(implicit impl: UFunc.SinkImpl2[Tag, S, V, V2]): S = { impl(__s, v, v2); __s }
    def apply[V, V2, V3](v: V, v2: V2, v3: V3)(implicit impl: UFunc.SinkImpl3[Tag, S, V, V2, V3]): S = {
      impl(__s, v, v2, v3); __s
    }
  }

}

case class WrappedUFunc1[A1, R](f: A1 => R) extends VariableUFunc[WrappedUFunc.type, WrappedUFunc1[A1, R]]

case class WrappedUFunc2[A1, A2, R](f: (A1, A2) => R) extends VariableUFunc[WrappedUFunc.type, WrappedUFunc2[A1, A2, R]]

object WrappedUFunc extends UFunc with WrappedUFuncLowPrio {

  implicit def simpleApply[A1, R]: Impl2[WrappedUFunc1[A1, R], A1, R] = { (v: WrappedUFunc1[A1, R], v2: A1) =>
    v.f(v2)
  }

  implicit def simpleApply2[A1, A2, R]: Impl3[WrappedUFunc2[A1, A2, R], A1, A2, R] = {
    (v: WrappedUFunc2[A1, A2, R], v2: A1, v3: A2) => v.f(v2, v3)
  }

  implicit def apply1[V, A1, R, V2](implicit cmv: CanMapValues[V, A1, R, V2]): Impl2[WrappedUFunc1[A1, R], V, V2] = {
    (v: WrappedUFunc1[A1, R], v2: V) => cmv.map(v2, v.f)
  }

  implicit def apply2[V, A1, R, V2](implicit
    cmv: CanZipMapValues[V, A1, R, V2]
  ): Impl3[WrappedUFunc2[A1, A1, R], V, V, V2] = { (v: WrappedUFunc2[A1, A1, R], v2: V, v3: V) =>
    cmv.map(v2, v3, v.f)
  }

  implicit def apply2a[V, A1, A2, R, V2](implicit
    cmv: CanMapValues[V, A1, R, V2]
  ): Impl3[WrappedUFunc2[A1, A2, R], V, A2, V2] = { (v: WrappedUFunc2[A1, A2, R], v2: V, v3: A2) =>
    cmv.map(v2, v.f(_, v3))
  }

}

trait WrappedUFuncLowPrio { self: WrappedUFunc.type =>
  implicit def apply2b[V, A1, A2, R, V2](implicit
    cmv: CanMapValues[V, A2, R, V2]
  ): Impl3[WrappedUFunc2[A1, A2, R], A1, V, V2] = { (v: WrappedUFunc2[A1, A2, R], v2: A1, v3: V) =>
    cmv.map(v3, v.f(v2, _))
  }
}
