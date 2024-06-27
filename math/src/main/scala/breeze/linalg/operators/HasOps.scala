package breeze.linalg.operators

import breeze.generic.MappingUFuncOps
import breeze.generic.ZeroPreservingUFuncOps
import breeze.linalg.BroadcastedOps
import breeze.linalg.SliceVectorOps

trait HasOps extends Any

object HasOps
    extends GenericOps
    with VectorOps
    with TensorLowPrio
    with TransposeOps
    with DenseVectorOps
    with SparseVectorOps
    with HashVectorOps
    with MatrixOps
    with DenseMatrixOps
    with CSCMatrixOps
    with SliceVectorOps
    with BitVectorOps
    with MappingUFuncOps
    with ZeroPreservingUFuncOps
    with BroadcastedOps {}
