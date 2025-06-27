<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Nouveau mot de passe - BigPharma</title>
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
        .reset-container {
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
        .password-strength {
            height: 5px;
            transition: width 0.3s;
            margin-top: 5px;
        }
    </style>
</head>
<body>
    <a href="/bigpharma/" class="back-to-home btn btn-outline-primary">
        <i class="fas fa-arrow-left"></i> Retour à l'accueil
    </a>
    
    <div class="reset-container">
        <h2 class="text-center mb-4">Nouveau mot de passe</h2>
        
        <?php if(isset($error)): ?>
            <div class="alert alert-danger">
                <?= htmlspecialchars($error) ?>
            </div>
        <?php endif; ?>

        <p class="text-muted mb-4">
            Veuillez saisir votre nouveau mot de passe ci-dessous.
        </p>

        <form method="POST" action="/bigpharma/password-reset/update" id="resetForm">
            <input type="hidden" name="token" value="<?= htmlspecialchars($token ?? '') ?>">
            
            <div class="mb-3">
                <label for="password" class="form-label">Nouveau mot de passe</label>
                <input 
                    type="password" 
                    class="form-control" 
                    id="password" 
                    name="password" 
                    required 
                    minlength="8"
                    placeholder="Votre nouveau mot de passe"
                >
                <div class="password-strength bg-danger" style="width: 0%"></div>
                <small class="form-text text-muted">
                    Le mot de passe doit contenir au moins 8 caractères, incluant des lettres majuscules, 
                    minuscules, des chiffres et des caractères spéciaux.
                </small>
            </div>
            
            <div class="mb-4">
                <label for="confirm_password" class="form-label">Confirmer le mot de passe</label>
                <input 
                    type="password" 
                    class="form-control" 
                    id="confirm_password" 
                    name="confirm_password" 
                    required 
                    placeholder="Confirmez votre nouveau mot de passe"
                >
                <div id="password-match-feedback"></div>
            </div>
            
            <button type="submit" class="btn btn-primary w-100" id="submitBtn" disabled>
                Mettre à jour le mot de passe
            </button>
        </form>
        
        <div class="text-center mt-3">
            <a href="/bigpharma/login" class="text-muted">Retour à la connexion</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Vérification de la force du mot de passe
        document.getElementById('password').addEventListener('input', function() {
            const password = this.value;
            const strengthBar = document.querySelector('.password-strength');
            const submitBtn = document.getElementById('submitBtn');
            
            // Critères de force
            const hasLowerCase = /[a-z]/.test(password);
            const hasUpperCase = /[A-Z]/.test(password);
            const hasNumber = /\d/.test(password);
            const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);
            const isLongEnough = password.length >= 8;
            
            // Calcul de la force (0-100)
            let strength = 0;
            if (hasLowerCase) strength += 20;
            if (hasUpperCase) strength += 20;
            if (hasNumber) strength += 20;
            if (hasSpecialChar) strength += 20;
            if (isLongEnough) strength += 20;
            
            // Mise à jour de la barre de force
            strengthBar.style.width = strength + '%';
            
            // Mise à jour de la couleur
            if (strength < 40) {
                strengthBar.className = 'password-strength bg-danger';
            } else if (strength < 80) {
                strengthBar.className = 'password-strength bg-warning';
            } else {
                strengthBar.className = 'password-strength bg-success';
            }
            
            // Vérifier si les mots de passe correspondent
            checkPasswordsMatch();
        });
        
        // Vérification que les mots de passe correspondent
        document.getElementById('confirm_password').addEventListener('input', checkPasswordsMatch);
        
        function checkPasswordsMatch() {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirm_password').value;
            const feedback = document.getElementById('password-match-feedback');
            const submitBtn = document.getElementById('submitBtn');
            
            if (confirmPassword === '') {
                feedback.innerHTML = '';
                submitBtn.disabled = true;
                return;
            }
            
            if (password === confirmPassword) {
                feedback.innerHTML = '<small class="text-success">Les mots de passe correspondent</small>';
                
                // Activer le bouton si le mot de passe est assez fort
                const strengthBar = document.querySelector('.password-strength');
                const strength = parseInt(strengthBar.style.width);
                submitBtn.disabled = strength < 60;
            } else {
                feedback.innerHTML = '<small class="text-danger">Les mots de passe ne correspondent pas</small>';
                submitBtn.disabled = true;
            }
        }
    </script>
</body>
</html>
