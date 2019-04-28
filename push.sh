git config –global user.name "HiedaNaKan"
git config –global user.email "HiedaNaKan@kurumi.io"
git config –global credential.helper store

git add .

if [ ! $1 ]; then

  git commit -m "一点微小的工作 ~"
  
else

  git commit -m $1
  
fi

git push
