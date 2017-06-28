<?php

$movie = $_GET['movie'];

switch ($movie) {
    case 'matrix':
        header("Location: matrix.html");
        break;
    case 'shining':
        header("Location: shining.html");
        break;
    case 'shaun_the_sheep':
        header("Location: http://www.shaunthesheep.com");
        break;
    case 'redirect':
        header("Location: /");
        break;

}

