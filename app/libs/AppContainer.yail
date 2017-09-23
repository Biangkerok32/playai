#|
$Source $Yail
|#

(define-repl-form com.jimx.playapp.AppContainer Screen1)
(require <com.google.youngandroid.runtime>)

(do-after-form-creation
 (set-and-coerce-property! 'Screen1 'AlignHorizontal 3 'number)
 (set-and-coerce-property! 'Screen1 'BackgroundColor #x00FFFFFF 'number)
 (set-and-coerce-property! 'Screen1 'Scrollable #t 'boolean)
 (set-and-coerce-property! 'Screen1 'Title "PlayApp" 'text)
)

(define-event Screen1 Initialize() (set-this-form)(call-component-method 'AppRenderer 'render (*list-for-runtime* ) '()))

(add-component Screen1 com.jimx.components.AppRenderer AppRenderer)

(init-runtime)

