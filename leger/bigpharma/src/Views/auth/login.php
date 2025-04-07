<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Connexion - BigPharma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        body { 
            background-color: #f4f6f9; 
            display: flex; 
            align-items: center; 
            justify-content: center; 
            height: 100vh; 
        }
        .login-container {
            background-color: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 400px;
        }
        .back-to-home {
            position: absolute;
            top: 20px;
            left: 20px;
        }
    </style>
</head>
<body>
    <a href="/bigpharma/public/" class="back-to-home btn btn-outline-primary">
        <i class="fas fa-arrow-left"></i> Retour à l'accueil
    </a>
    
    <div class="login-container">
        <h2 class="text-center mb-4">Connexion Pharmacie</h2>
        
        <?php if(isset($error)): ?>
            <div class="alert alert-danger">
                <?= htmlspecialchars($error) ?>
            </div>
        <?php endif; ?>

        <?php if(isset($_SESSION['reset_success'])): ?>
            <div class="alert alert-success">
                <?= htmlspecialchars($_SESSION['reset_success']) ?>
                <?php unset($_SESSION['reset_success']); ?>
            </div>
        <?php endif; ?>

        <form method="POST" action="/bigpharma/login/process">
            <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input 
                    type="email" 
                    class="form-control" 
                    id="email" 
                    name="email" 
                    required 
                    placeholder="Votre email professionnel"
                >
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">Mot de passe</label>
                <input 
                    type="password" 
                    class="form-control" 
                    id="password" 
                    name="password" 
                    required 
                    placeholder="Votre mot de passe"
                >
            </div>
            <div class="mb-3 form-check">
                <input type="checkbox" class="form-check-input" id="remember" name="remember">
                <label class="form-check-label" for="remember">Se souvenir de moi</label>
            </div>
            <button type="submit" class="btn btn-primary w-100">Se connecter</button>
        </form>
        
        <div class="text-center mt-3">
            <a href="/bigpharma/register" class="text-muted">Créer un compte</a> | 
            <a href="/bigpharma/password-reset" class="text-muted">Mot de passe oublié</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
