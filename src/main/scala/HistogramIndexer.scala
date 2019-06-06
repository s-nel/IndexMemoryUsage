import model.Histogram

trait HistogramIndexer[F[_]] {
  def index(histogram: Histogram): F[Unit]
}
