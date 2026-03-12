import Distribution.Compat.Lens (_1)
data Expr = S | K | I | B | Expr :$ Expr | X | Z | V Int deriving(Show, Read);
infixl 9 :$
-- function types

-- tests
test1 = S :$ K :$ K :$ X
twoB = S :$B :$ I
threeB = S :$ B :$ (S :$B :$ I)
test3 = threeB :$ X :$ Z
omega = ((S :$ I) :$ I) :$ ((S :$ I) :$ I)
kio = K :$ I :$ omega
add = (B :$ S) :$ (B :$ B)

prettyExprInside :: Expr -> Maybe Bool -> String
-- operations
prettyExprInside S _ = "S"
prettyExprInside K _ = "K"
prettyExprInside I _ = "I"
prettyExprInside B _ = "B"
-- arguments 
prettyExprInside X _ = "x"
prettyExprInside Z _ = "z"
prettyExprInside (V n) _ = "v" ++ show n

-- Expr :$ Expr
prettyExprInside (e1 :$ e2) is_right = (if is_right == Just True then "(" else "") ++ prettyExprInside e1 Nothing ++ " " ++ prettyExprInside e2 (Just True) ++ if is_right == Just True then ")" else ""

-- main function for printing expresions
prettyExpr :: Expr -> String
prettyExpr e = prettyExprInside e  Nothing

-- reduction step
-- rstep :: Expr-> Expr
-- colection of all subexpressions that we need to check for reduction first
-- collectExpr :: Expr -> [Expr]
-- collectExpr e = case e of
--         S -> [S]
--         K -> [K]
--         I -> [I]
--         B -> [B]
--         X -> [X]
--         Z -> [Z]
--         V n -> [V n]
--         e1 :$ e2 -> collectExpr e1 ++ [e2]

-- -- making the reduction from the list of expressions and transforming to the expresion
-- transformExpr :: [Expr] -> [Expr]
-- transformExpr (S : e1 : e2 : e3 : rest) = [e1 :$ e3, e2 :$ e3] ++ rest
-- transformExpr (K : e1 : e2 : rest) = e1 : rest
-- transformExpr (I : e1 : rest) = e1 : rest
-- transformExpr (B : e1 : e2 : e3 : rest) = e1 : (e2 :$ e3) : rest
-- transformExpr (e : rest) = e : transformExpr rest
-- transformExpr [] = []

reduceDefleating :: Expr -> Bool -> Expr
reduceDefleating (I :$ x) b = if b then x else I :$ x
reduceDefleating (K :$ x :$ y)  b = if b then x else K :$ x :$ y
reduceDefleating (B :$ f :$ g :$ x)  b = if b then f :$ (g :$ x) else B :$ f :$ g :$ x
reduceDefleating e b = e

rstep :: Expr -> Bool -> Expr
rstep (S :$ f :$ g :$ x) b = (reduceDefleating f b :$ reduceDefleating x b) :$ (reduceDefleating g b :$ reduceDefleating x b)
rstep (K :$ x :$ _) b = reduceDefleating x b
rstep (I :$ x) b = reduceDefleating x b
rstep (B :$ f :$ g :$ x) b = reduceDefleating f b :$ ( reduceDefleating g b :$  reduceDefleating x b)
rstep (X :$ e) b = X :$ rstep e b
rstep (Z :$ e) b = Z :$ rstep e b
rstep (V n :$ e) b = V n :$ rstep e b
rstep (e1 :$ e2) b = rstep e1 b :$  e2    -- <-- recurse into left spine
rstep e b = e

rpath :: Expr -> Integer -> [Expr]
rpath e 0 = [e]
rpath e n = e : if show e == show r then [] else rpath r (n - 1)
    where r = rstep e False

printPath :: Expr -> IO ()

printPath e = putStrLn $ unlines $ map prettyExpr (rpath e 30)

printPath' :: Expr -> IO ()


rpath' :: Expr -> Integer -> [Expr]
rpath' e 0 = [e]
rpath' e n = e : if show e == show r then [] else rpath' r (n - 1)
    where r = rstep e True

printPath' e = putStrLn $ unlines $ map prettyExpr (rpath' e 30)
