/*
 * Copyright 2017 Jan Bessai
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
 */

package org.combinators.cls.git

import play.api.mvc.InjectedController

/** Cleans trailing double slashes of urls.
  * Turns `http://foo.bar/baz/` into `http://foo.bar/baz`.
  * To do anything useful this requires the following routing entry (present in [[org.combinators.cls.git.Routes]]):
  * {{{
  *   GET &sol;*path&sol; org.combinators.cls.git.URLCleanup.untrail(path)
  * }}}
  */
class URLCleanup extends InjectedController {
  def untrail(path: String) = Action {
    MovedPermanently("/" + path)
  }
}
