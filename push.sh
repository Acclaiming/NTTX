git pull
git add .

if [ ! $1 ]; then

  git commit -m "喵"
  
else

  git commit -m $1
  
fi

git push
