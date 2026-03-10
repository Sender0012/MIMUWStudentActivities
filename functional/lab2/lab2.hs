
elimMaybe :: c -> (a -> c) -> Maybe a -> c
fromMaybe :: a -> Maybe a -> a
mapMaybe :: (a -> b) -> Maybe a -> Maybe b
maybeHead :: [a] -> Maybe a
elimEither :: (a  -> c) -> (b -> c) -> Either a b -> c
mapEither :: (a1 -> a2) -> (b1 -> b2) -> Either a1 b1 -> Either a2 b2
-- mapRight ::  (b1 -> b2) -> Either a b1 -> Either a b2
-- fromEither :: Either a a -> a

fromMaybe _ (Just x) = x
fromMaybe f _ = f

elimMaybe x f (Just y) = f y
elimMaybe x f _ = x


mapMaybe f (Just x) = Just (f x)
mapMaybe _ _ = Nothing

maybeHead l = 
    case l of
        [] -> Nothing
        l: _ -> Just l

elimEither f g (Left x) = f x
elimEither f g (Right y) = g y

mapEither f g (Left x) = Left (f x)
mapEither f g (Right y) = Right (g y)

fromEither (Left x) = x
fromEither (Right x) = x

both :: (a -> b) -> (a, a) -> (b, b)
all3 :: (a -> b) -> (a, a, a) -> (b, b, b)
first :: (a -> c) -> (a, b) -> (c, b)
second :: (b -> d) -> (a, b) -> (a, d)

both f (x, y) = (f x, f y)
all3 f (x, y, z) = (f x, f y, f z)
first f (x, y) = (f x, y)
second f (x, y) = (x, f y)


dziel :: Int -> [Int]

dziel n = [x | x <- [1..n], n `mod` x == 0]

isPrime :: Int -> Bool
isPrime n = dziel n == [1, n]



-- drzewa
data Tree a = Empty | Node a (Tree a) (Tree a) deriving (Eq, Ord)

-- This do not require the Eq constraint
-- instance Show a => Show (Tree a) where
--   show Empty = "."
--   show (Node x Empty Empty) = show x
--   show (Node x l r) = "(" ++ show x ++ show l ++ show r ++ ")"

instance (Show a, Eq a) => Show (Tree a) where
  show Empty = "."
  show (Node x l r) =
    if l == Empty && r == Empty
      then show x
      else "(" ++ show x ++  " " ++ show l ++ " " ++ show r ++ ")"

fullTree :: Int -> Tree Int
changeNodeValue :: Tree a -> (a->a)-> Tree a
changeNodeValue Empty _ = Empty
changeNodeValue (Node y l r) f = Node (f y) (changeNodeValue l f) (changeNodeValue r f)



fullTree 0 = Empty
-- fullTree n = Node (2^(n-1)) (fullTree (n-1)) (changeNodeValue (fullTree (n-1)) (+(2^(n-1))))

-- calculating once the subtree and the using it twice, once with the original values and once with the changed values
-- fullTree n = 
--   let subtree = fullTree (n-1)
--   in Node (2^(n-1)) subtree (changeNodeValue subtree (+(2^(n-1))))

fullTree n = Node (2^(n-1)) subtree (changeNodeValue subtree (+(2^(n-1))))
  where subtree = fullTree (n-1)
toList :: Tree a -> [a]

toList Empty = []
toList (Node x l r) = toList l ++ [x] ++ toList r


fullTreeFrom :: Int -> Int -> Tree Int

fullTreeFrom 0 _ = Empty
fullTreeFrom m n = changeNodeValue (fullTree n) (+m)