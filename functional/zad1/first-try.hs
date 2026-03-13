-- Jakub Senderowski IND: 459491 gr. 5
infixl 9 :$
data Expr = S | K | I | B | Expr :$ Expr | X | Z | V Int deriving (Show, Read, Eq)

-- tests
test1 = S :$ K :$ K :$ X
twoB = S :$B :$ I
threeB = S :$ B :$ (S :$B :$ I)
test3 = threeB :$ X :$ Z
omega = ((S :$ I) :$ I) :$ ((S :$ I) :$ I)
kio = K :$ I :$ omega
add = (B :$ S) :$ (B :$ B)

-- omega-like non-terminating tests that include K and/or B
w = S :$ I :$ I
wK = S :$ (K :$ w) :$ I
omegaK = wK :$ wK
wB = S :$ (B :$ I :$ I) :$ I
omegaB = wB :$ wB
qKB = B :$ w :$ I :$ w
wKB = S :$ (K :$ qKB) :$ I
omegaKB = wKB :$ wKB

prettyExprInside :: Expr -> Bool -> String
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
prettyExprInside (e1 :$ e2) needsParens =
        wrap needsParens (prettyExprInside e1 False ++ " " ++ prettyExprInside e2 True)
    where
        wrap True s = "(" ++ s ++ ")"
        wrap False s = s

-- main function for printing expresions
prettyExpr :: Expr -> String
prettyExpr e = prettyExprInside e  False

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
-- reduction rules
rstep (S :$ f :$ g :$ x) b = (reduceDefleating f b :$ reduceDefleating x b) :$ (reduceDefleating g b :$ reduceDefleating x b)
rstep (K :$ x :$ _) b =  x
rstep (I :$ x) b =  x
rstep (B :$ f :$ g :$ x) b =  f :$ (  g :$   x)
-- if the leftmost combinator was not reduced we need to check the right one
rstep (e1 :$ e2) b = 
    let e1' = rstep e1 b 
    in if e1' /= e1 
        then e1' :$ e2
        else 
            let e2' = rstep e2 b 
            in e1 :$ e2' -- e1 == e1' so we can check e2
-- if there is no reduction we return the same expression
rstep e b = e

-- function to compute the path of the reduction with a limit on the number of steps
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
