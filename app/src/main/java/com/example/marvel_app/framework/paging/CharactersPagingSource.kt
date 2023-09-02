package com.example.marvel_app.framework.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.core.features.characters.data.datasource.CharactersRemoteDatasource
import com.example.core.features.characters.domain.model.Character
import com.example.marvel_app.features.characters.response.characters.DataWrapperResponse
import com.example.marvel_app.features.characters.response.characters.toCharacterModel
import com.example.marvel_app.utils.Constants.LIMIT

class CharactersPagingSource(
    private val charactersRemoteDatasource: CharactersRemoteDatasource<DataWrapperResponse>,
    private val query: String
) : PagingSource<Int, Character>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {

        return try{
            val offset = params.key ?: 0

            val queries = hashMapOf(
                "offset" to offset.toString()
            )

            if (query.isNotEmpty()) {
                queries["nameStartsWith"] = query
            }

            val response = charactersRemoteDatasource.fetchCharacters(queries)

            val responseOffset = response.data.offset
            val totalCharacters = response.data.total

            LoadResult.Page(
                data = response.data.results.map { it.toCharacterModel() },
                prevKey = null,
                nextKey = if (responseOffset < totalCharacters) {
                    responseOffset + LIMIT
                } else null
            )
        } catch (exception: Exception){
            LoadResult.Error(exception)
        }

    }

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(LIMIT) ?: anchorPage?.nextKey?.minus(LIMIT)
        }
    }
}