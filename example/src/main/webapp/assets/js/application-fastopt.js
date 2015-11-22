(function(){
'use strict';
/* Scala.js runtime support
 * Copyright 2013 LAMP/EPFL
 * Author: Sébastien Doeraene
 */
/* ---------------------------------- *
 * The top-level Scala.js environment *
 * ---------------------------------- */
// Get the environment info
var $env = (typeof __ScalaJSEnv === "object" && __ScalaJSEnv) ? __ScalaJSEnv : {};
// Global scope
var $g =
  (typeof $env["global"] === "object" && $env["global"])
    ? $env["global"]
    : ((typeof global === "object" && global && global["Object"] === Object) ? global : this);
$env["global"] = $g;
// Where to send exports
var $e =
  (typeof $env["exportsNamespace"] === "object" && $env["exportsNamespace"])
    ? $env["exportsNamespace"] : $g;
$env["exportsNamespace"] = $e;
// Freeze the environment info
$g["Object"]["freeze"]($env);
}).call(this);
//# sourceMappingURL=application-fastopt.js.map
