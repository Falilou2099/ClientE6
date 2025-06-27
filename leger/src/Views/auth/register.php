<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Inscription - BigPharma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        body { 
            background-color: #f4f6f9; 
            display: flex; 
            align-items: center; 
            justify-content: center; 
            min-height: 100vh; 
            padding: 20px 0;
        }
        .register-container {
            background-color: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 600px;
        }
        .back-to-home {
            position: absolute;
            top: 20px;
            left: 20px;
        }
    </style>
</head>
<body>
    <a href="/bigpharma/" class="back-to-home btn btn-outline-primary">
        <i class="fas fa-arrow-left"></i> Retour à l'accueil
    </a>
    
    <div class="register-container">
        <h2 class="text-center mb-4">Inscription Pharmacie</h2>
        <p class="text-center text-muted mb-4">Créez un compte unique pour accéder aux applications légère et lourde</p>
        
        <?php if(isset($errors)): ?>
            <div class="alert alert-danger">
                <?php foreach($errors as $error): ?>
                    <p><?= htmlspecialchars($error) ?></p>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>

        <form method="POST" action="/bigpharma/register/process">
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="pharmacyName" class="form-label">Nom de la Pharmacie</label>
                    <input 
                        type="text" 
                        class="form-control" 
                        id="pharmacyName" 
                        name="pharmacy_name" 
                        required 
                        placeholder="Nom officiel de la pharmacie"
                    >
                </div>
                <div class="col-md-6 mb-3">
                    <label for="registrationNumber" class="form-label">Numéro d'Enregistrement</label>
                    <input 
                        type="text" 
                        class="form-control" 
                        id="registrationNumber" 
                        name="registration_number" 
                        required 
                        placeholder="Numéro RPPS"
                    >
                </div>
            </div>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="email" class="form-label">Email Professionnel</label>
                    <input 
                        type="email" 
                        class="form-control" 
                        id="email" 
                        name="email" 
                        required 
                        placeholder="email@pharmacie.fr"
                    >
                </div>
                <div class="col-md-6 mb-3">
                    <label for="phoneNumber" class="form-label">Numéro de Téléphone</label>
                    <input 
                        type="tel" 
                        class="form-control" 
                        id="phoneNumber" 
                        name="phone_number" 
                        required 
                        placeholder="0612345678"
                    >
                </div>
            </div>

            <div class="mb-3">
                <label for="address" class="form-label">Adresse de la Pharmacie</label>
                <input 
                    type="text" 
                    class="form-control" 
                    id="address" 
                    name="address" 
                    required 
                    placeholder="123 Rue de la Santé, 75001 Paris"
                >
            </div>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="password" class="form-label">Mot de passe</label>
                    <input 
                        type="password" 
                        class="form-control" 
                        id="password" 
                        name="password" 
                        required 
                        placeholder="Minimum 8 caractères"
                        minlength="8"
                    >
                </div>
                <div class="col-md-6 mb-3">
                    <label for="confirmPassword" class="form-label">Confirmer le Mot de passe</label>
                    <input 
                        type="password" 
                        class="form-control" 
                        id="confirmPassword" 
                        name="confirm_password" 
                        required 
                        placeholder="Répétez le mot de passe"
                        minlength="8"
                    >
                </div>
            </div>

            <div class="mb-3">
                <label for="app_access" class="form-label">Type d'accès à l'application</label>
                <select class="form-select" id="app_access" name="app_access">
                    <option value="both" selected>Les deux applications (légère et lourde)</option>
                    <option value="light">Application légère (Web) uniquement</option>
                    <option value="heavy">Application lourde (Java) uniquement</option>
                </select>
                <div class="form-text">Sélectionnez le type d'application auquel vous souhaitez avoir accès</div>
            </div>
            
            <div class="mb-3 form-check">
                <input 
                    type="checkbox" 
                    class="form-check-input" 
                    id="termsConditions" 
                    name="terms" 
                    required
                >
                <label class="form-check-label" for="termsConditions">
                    J'accepte les conditions générales d'utilisation
                </label>
            </div>

            <button type="submit" class="btn btn-primary w-100">S'inscrire</button>
        </form>
        
        <div class="text-center mt-3">
            <p class="text-muted">Déjà inscrit ? <a href="/bigpharma/login">Connectez-vous</a></p>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.querySelector('form').addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                alert('Les mots de passe ne correspondent pas');
            }
        });
    </script>
</body>
</html>
