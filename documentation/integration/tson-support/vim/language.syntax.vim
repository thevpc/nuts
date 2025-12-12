" tson.vim – Syntax highlighting for NTexUp

if exists("b:current_syntax")
  finish
endif

" Comments
syn match tsonLineComment "//.*$"
syn region tsonBlockComment start="/\*" end="\*/" contains=tsonTodo
syn keyword tsonTodo TODO FIXME XXX BUG

" Strings
syn region tsonString start=+"+ skip=+\\"+ end=+"+
syn region tsonString start=+'+ skip=+\\'+ end=+'+

" Numbers
syn match tsonNumber "\v(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?"

" Symbols
syn match tsonSymbol "[:(){}\[\];,+\-*/=<>!|&]"


" Highlight Groups Mapping
hi def link tsonLineComment   Comment
hi def link tsonBlockComment  Comment
hi def link tsonTodo          Todo
hi def link tsonString        String
hi def link tsonNumber        Number
hi def link tsonSymbol        Operator

hi def link tsonKwd1          Keyword
hi def link tsonKwd2          Type
hi def link tsonAttr          Identifier
hi def link tsonDoc           Structure

let b:current_syntax = "tson"
