# PAM
Projet de programmation d'application mobile M2 MIAGE GILLIARD Paul, TOBLER Lola

Vous trouverez une vidéo de présentation de notre projet via le lien suivant (youtube) : https://youtu.be/NZ5pikHFxtI


L'objectif de ce projet est de créer une application-jeu avec Android Studio. Le but de cette application-jeu est de pouvoir se déplacer sur une carte (géolocalisation) afin d'attraper des objets. Lorsque l'utilisateur s'approche d'un objet, représenter par un point sur la carte, il pourra accèder à sa caméra afin de visualiser l'objet dans son environnement. Il pourra ensuite capture l'objet, ce qui le fera disparaitre de la carte. 

Synopsis : 
Bienvenue dans l'application "Frog Go"!
Vous êtes à la recherche de la grenouille ultime? Vous avez trouvé le bon endroit. Avec Frog Go, vous pouvez parcourir votre quartier à la recherche de grenouilles sauvages à capturer.
Pour capturer une grenouille, suivez simplement ces étapes:
- Ouvrez l'application et regardez autour de vous pour trouver une grenouille à proximité.
- Une fois que vous avez repéré une grenouille, approchez-vous doucement et ouvrez votre appareil photo en utilisant le bouton "caméra".
- Prenez une photo de votre grenouille, elle disparaitra ainsi de votre maps.
Alors, qu'attendez-vous? Sortez et capturez toutes les grenouilles de votre quartier!


INSTALLATION
Pour utiliser l'application sur votre téléphone, vous pouvez installer dans votre téléphone le fichier APK déposé dans le Github.


Elements réussis du projet
- Demander les autorisations au lancement de l'application pour géolocaliser l'utilisateur et utiliser sa caméra
- Ouvrir l'API de google maps en situant la localisation de l'utilisateur
- Placer des points représentant l'emplacement des grenouilles
- Mise en place d'un bouton "caméra" sur la map qui permet d'accèder à l'appareil photo
- Supperposition de l'image de la grenouille avec l'image de l'appareil photo
- Suppression de la grenouille sur la maps une fois qu'elle a été prise en photo
- Activation du bouton "Caméra" disponible uniquement lorsque l'utilisateur est proche de la grenouille
 
Elements pas réussis du projet
- Compteur de grenouilles capturées
- Utilisation de l'orientation (nord sud) pour faire apparaitre ou non la grenouille sur l'appareil photo



Dans l'état actuel du projet, au lancement de l'application deux coordonnées GPS de grenouilles sont générées (Les deux points bleus sur la map). La première grenouille est assez proche pour être capturée c'est pour cela que le bouton de la caméra est enable. La seconde est trop loin. Une fois que nous avons capturé la première, cela supprime des coordonées stockées la première grenouille (c'est pour cela qu'elle n'apparait plus sur la Map) et nous nous retrouvons uniquement avec la seconde grenouille qui est trop loin pour être capturée.
A noter que vous pouvez re générer le fichier de coordonées avec les deux grenouilles simplement en quittant complètement l'application et en la relançant.
Ces coordonnées GPS sont établies en fonction de votre position actuelle en leur ajoutant une petite valeur de latitude et de longitude chacune. 
