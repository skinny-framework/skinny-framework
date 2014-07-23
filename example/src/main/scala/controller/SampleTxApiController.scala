package controller

import skinny._
import skinny.filter.TxPerRequestFilter

class SampleTxApiController extends SkinnyApiController with TxPerRequestFilter {

  def index = throw new RuntimeException("test")

}