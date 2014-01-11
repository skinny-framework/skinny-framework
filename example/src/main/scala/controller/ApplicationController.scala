package controller

import skinny.SkinnyController
import skinny.filter._

trait ApplicationController extends SkinnyController
    with SkinnyFilterActivation
    with ErrorPageFilter {

}
