git config user.name "稗田 奈间"
git config user.email "HiedaNaKan@kurumi.io"
git config credential.helper store

git add .

if [ ! $0 ]; then

  git commit -m "一点微小的工作 ~"
  
else

  git commit -m $0
  
fi

git push
