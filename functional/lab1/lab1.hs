import Distribution.Simple.Utils (xargs)
sayhello x = print ("Hello, " ++ x)
main = sayhello "World"

i x = x

s x y z = x z (y z)

k x y = x

b x y z = x (y z)
zero _ x = x

suc n f x = f (n f x)

add m n f x = m f (n f x)

two = suc (suc zero)

three = suc two

one  = suc zero
-- mno :: ((t1 -> t2 -> t3) -> t1 -> t3 -> t4) -> (t1 -> t2 -> t3) -> t1 -> t2 -> t4




mno m n f x= m (n f x)

runNat n = n (+1) 0


funNat n = n ('|':) ""
