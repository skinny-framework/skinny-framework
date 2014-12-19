package skinny.oauth2.client.dropbox

case class QuotaInfo(
  datastores: Long,
  shared: Long,
  quota: Long,
  normal: Long)
