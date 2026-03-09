

data Expr = S | K | I | B | Expr :$ Expr | X | Z | V Int deriving(Show, Read);

prettyExprInside :: Expr -> Maybe Bool -> String
prettyExpr :: Expr -> String

prettyExprInside S _ = "S"
prettyExprInside K _ = "K"
prettyExprInside I _ = "I"
prettyExprInside B _ = "B"

prettyExprInside X _ = "x"
prettyExprInside Z _ = "z"
prettyExprInside (V n) _ = "v" ++ show n

prettyExprInside (e1 :$ e2) is_right = (if is_right == Just True then "(" else "") ++ prettyExprInside e1 Nothing ++ " " ++ prettyExprInside e2 (Just True) ++ if is_right == Just True then ")" else ""

prettyExpr e = prettyExprInside e  Nothing

test1 = S :$ K :$ K :$ X
twoB = S :$B :$ I
threeB = S :$ B :$ (S :$B :$ I)
test3 = threeB :$ X :$ Z
omega = ((S :$ I) :$ I) :$ ((S :$ I) :$ I)
kio = K :$ I :$ omega
add = (B :$ S) :$ (B :$ B)


