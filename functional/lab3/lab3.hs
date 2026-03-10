data Exp
    = EInt Int  -- inny typ od integer             -- stała całkowita
    | EAdd Exp Exp         -- e1 + e2
    | EMul Exp Exp         -- e1 * e2
    | EVar String          -- zmienna
    | ELet String Exp Exp  -- let var = e1 in e2
    deriving (Show, Eq)

type Env = [(String, Int)]
fromInteger :: Integer -> Int
fromInteger = Prelude.fromInteger

