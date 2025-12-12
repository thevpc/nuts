" ntf.vim - Syntax highlighting for Nuts Text Format (NTF)
if exists("b:current_syntax")
  finish
endif

" Verbatim/Code blocks
syn region ntfCodeBlock start=+```+ end=+```+ contains=ntfCodeLang
syn match ntfCodeLang "\v```(xml|json|java|sh|bash|js|javascript|sql|python|c|cpp|cs|csharp|nsh)" contained

" Titles at line start
syn match ntfTitle1 "^#)" nextgroup=ntfTitleText skipwhite
syn match ntfTitle2 "^##)" nextgroup=ntfTitleText skipwhite
syn match ntfTitle3 "^###)" nextgroup=ntfTitleText skipwhite
syn match ntfTitle4 "^####)" nextgroup=ntfTitleText skipwhite
syn match ntfTitle5 "^#####)" nextgroup=ntfTitleText skipwhite
syn match ntfTitle6 "^######)" nextgroup=ntfTitleText skipwhite
syn match ntfTitle7 "^#######)" nextgroup=ntfTitleText skipwhite
syn match ntfTitle8 "^########)" nextgroup=ntfTitleText skipwhite
syn match ntfTitle9 "^#########)" nextgroup=ntfTitleText skipwhite

" Hash styles ##Text## to ##########Text##########
syn region ntfHashStyle2 start="##\ze[^#:]" end="##" contains=ntfHashContent oneline
syn region ntfHashStyle3 start="###\ze[^#]" end="###" contains=ntfHashContent oneline
syn region ntfHashStyle4 start="####\ze[^#]" end="####" contains=ntfHashContent oneline
syn region ntfHashStyle5 start="#####\ze[^#]" end="#####" contains=ntfHashContent oneline
syn region ntfHashStyle6 start="######\ze[^#]" end="######" contains=ntfHashContent oneline
syn region ntfHashStyle7 start="#######\ze[^#]" end="#######" contains=ntfHashContent oneline
syn region ntfHashStyle8 start="########\ze[^#]" end="########" contains=ntfHashContent oneline
syn region ntfHashStyle9 start="#########\ze[^#]" end="#########" contains=ntfHashContent oneline
syn region ntfHashStyle10 start="##########" end="##########" contains=ntfHashContent oneline

" Color/Style markup ##:style:Text##
syn region ntfColorStyle start="##:" end="##" contains=ntfStyleName,ntfColorName,ntfColorCode,ntfStyleOperator oneline

" Style names
syn keyword ntfStyleName contained underlined italic striked reversed bold
syn keyword ntfStyleName contained error warn info config comments string number boolean
syn keyword ntfStyleName contained keyword option input operator separator success danger fail var pale

" Color keywords
syn keyword ntfColorName contained primary secondary foreground background

" Color codes
syn match ntfColorCode contained "\v[psfb]\d+"
syn match ntfColorCode contained "\v[psfb]x[0-9A-Fa-f]{6}"

" Style operators
syn match ntfStyleOperator contained "[:/_{%!+\-}]"

" Special characters
syn match ntfEscape "\\[#\\`{}:u]"
syn match ntfNopChar "\%u001E"

" Highlight Groups Mapping
hi def link ntfCodeBlock       String
hi def link ntfCodeLang         Keyword

hi def link ntfTitle1           Title
hi def link ntfTitle2           Title
hi def link ntfTitle3           Title
hi def link ntfTitle4           Title
hi def link ntfTitle5           Title
hi def link ntfTitle6           Title
hi def link ntfTitle7           Title
hi def link ntfTitle8           Title
hi def link ntfTitle9           Title

hi def link ntfHashStyle2       Identifier
hi def link ntfHashStyle3       Identifier
hi def link ntfHashStyle4       Identifier
hi def link ntfHashStyle5       Identifier
hi def link ntfHashStyle6       Identifier
hi def link ntfHashStyle7       Identifier
hi def link ntfHashStyle8       Identifier
hi def link ntfHashStyle9       Identifier
hi def link ntfHashStyle10      Identifier
hi def link ntfHashContent      Normal

hi def link ntfColorStyle       Special
hi def link ntfStyleName        Type
hi def link ntfColorName        Type
hi def link ntfColorCode        Number
hi def link ntfStyleOperator    Operator

hi def link ntfEscape           SpecialChar
hi def link ntfNopChar          SpecialChar

let b:current_syntax = "ntf"
