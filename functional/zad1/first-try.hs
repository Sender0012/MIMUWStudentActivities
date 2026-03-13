-- Jakub Senderowski IND: 459491 gr. 5
infixl 9 :$
data Expr = S | K | I | B | Expr :$ Expr | X | Z | V Int deriving (Show, Read)

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
wK = S :$ (K :$ w :$ I) :$ I
omegaK = wK :$ wK
wB = S :$ (B :$ I :$ I) :$ I
omegaB = wB :$ wB
qKB = B :$ w :$ I :$ w
wKB = S :$ (K :$ qKB) :$ I
omegaKB = wKB :$ wKB

-- pretty printing of expressions
-- bool is needed to determine if we need to put parentheses or not
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

-- reduction rules for defleating the expression before reduction
-- True -> reduce
-- False -> do not reduce
reduceDefleating :: Expr -> Bool ->  Expr
reduceDefleating (I :$ x) b = if b then x else I :$ x
reduceDefleating (K :$ x :$ y)  b = if b then x else K :$ x :$ y
reduceDefleating (B :$ f :$ g :$ x)  b = if b then f :$ (g :$ x) else B :$ f :$ g :$ x
reduceDefleating e b = e

-- function to compute one step of reduction
rstep :: Expr -> Bool -> Maybe Expr
-- reduction rules
rstep (S :$ f :$ g :$ x) b = Just ((reduceDefleating f b :$ reduceDefleating x b) :$ (reduceDefleating g b :$ reduceDefleating x b))
rstep (K :$ x :$ _) _ =  Just x
rstep (I :$ x) _ =  Just x
rstep (B :$ f :$ g :$ x) _ =  Just (f :$ (g :$ x))
-- if the leftmost combinator was not reduced we need to check the right one
rstep (e1 :$ e2) b = case rstep e1 b of
    Just e1' -> Just (e1' :$ e2)
    Nothing -> case rstep e2 b of
        Just e2' -> Just (e1 :$ e2')
        Nothing -> Nothing
-- if there is no reduction we inform the caller by returning Nothing
rstep _ _ = Nothing

-- function to compute the path of the reduction with a limit on the number of steps
rpath :: Expr -> Integer -> [Expr]
rpath e 0 = [e]
rpath e n = e : case rstep e False of
    Just e' -> rpath e' (n - 1)
    Nothing -> [] -- if there is no reduction we stop the path


-- main function for printing the path of reduction
-- current limit of steps is 30, but it can be changed by changing the second argument of rpath
printPath :: Expr -> IO ()
printPath e = putStrLn $ unlines $ map prettyExpr (rpath e 30)

-- the same as rpath but with defleating before reduction
rpath' :: Expr -> Integer -> [Expr]
rpath' e 0 = [e]
rpath' e n = e : case rstep e True of
    Just e' -> rpath' e' (n - 1)
    Nothing -> []

-- the same as printPath but with defleating before reduction
printPath' :: Expr -> IO ()
printPath' e = putStrLn $ unlines $ map prettyExpr (rpath' e 30)
