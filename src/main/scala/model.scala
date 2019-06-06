object model {
  final case class InstanceId(value: String) extends AnyVal

  final case class InstanceData(key: String, value: String)

  final case class Histogram(instanceId: InstanceId, entries: Set[HistogramEntry], instanceData: Set[InstanceData])

  final case class HistogramEntry(instanceCount: Long, totalSize: Long, className: String) {
    def sizePerInstance(): Float = totalSize/instanceCount.toFloat
  }
}
