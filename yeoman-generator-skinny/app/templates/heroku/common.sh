# Common bash functions for Heroku

# Set up DB-related environment variables
function setup_db_env_vars {
    if [[ $DATABASE_URL =~ ^postgres.* ]]; then
        export DATABASE_USER=$(echo $DATABASE_URL | sed -e 's/^postgres:\/\/\(.*\):.*@.*:.*\/.*$/\1/')
        echo "DB user = $DATABASE_USER"

        export DATABASE_PASSWORD=$(echo $DATABASE_URL | sed -e 's/^postgres:\/\/.*:\(.*\)@.*:.*\/.*$/\1/')
        [ "$DATABASE_PASSWORD" == "" ] && echo "DB password NOT defined"
        [ "$DATABASE_PASSWORD" != "" ] && echo "DB password is defined"

        export DATABASE_HOST=$(echo $DATABASE_URL | sed -e 's/^postgres:\/\/.*:.*@\(.*\):.*\/.*$/\1/')
        echo "DB host = $DATABASE_HOST"

        export DATABASE_PORT=$(echo $DATABASE_URL | sed -e 's/^postgres:\/\/.*:.*@.*:\(.*\)\/.*$/\1/')
        echo "DB port = $DATABASE_PORT"

        export DATABASE_DBNAME=$(echo $DATABASE_URL | sed -e 's/^postgres:\/\/.*:.*@.*:.*\/\(.*\)$/\1/')
        echo "DB name = $DATABASE_DBNAME"
    fi
}

